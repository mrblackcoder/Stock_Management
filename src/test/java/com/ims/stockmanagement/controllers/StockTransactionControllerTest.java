package com.ims.stockmanagement.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.exceptions.GlobalExceptionHandler;
import com.ims.stockmanagement.exceptions.InsufficientStockException;
import com.ims.stockmanagement.services.StockTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockTransactionControllerTest {

    private static final String INSUFFICIENT_STOCK_MESSAGE =
            "Insufficient stock for product: Test Product. Available: 2, Requested: 3";
    private static final String LOCK_CONFLICT_MESSAGE =
            "Stock is currently being updated. Please retry your request.";
    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred.";
    private static final String DATA_CONFLICT_MESSAGE = "The operation conflicts with existing data.";

    @Mock
    private StockTransactionService transactionService;

    @InjectMocks
    private StockTransactionController transactionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private TransactionRequest validSaleRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(1L);
        request.setTransactionType(TransactionType.SALE);
        request.setQuantity(3);
        return request;
    }

    @Test
    void insufficientStockReturns422() throws Exception {
        TransactionRequest request = validSaleRequest();
        when(transactionService.saleProduct(any(TransactionRequest.class)))
                .thenThrow(new InsufficientStockException(INSUFFICIENT_STOCK_MESSAGE));

        mockMvc.perform(post("/api/transactions/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(422))
                .andExpect(jsonPath("$.message").value(INSUFFICIENT_STOCK_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(transactionService, times(1)).saleProduct(any(TransactionRequest.class));
    }

    @Test
    void pessimisticLockConflictReturns409() throws Exception {
        TransactionRequest request = validSaleRequest();
        String rawLockMessage = "lock wait timeout; SQL and database details must not be returned";
        when(transactionService.saleProduct(any(TransactionRequest.class)))
                .thenThrow(new CannotAcquireLockException(rawLockMessage));

        mockMvc.perform(post("/api/transactions/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value(LOCK_CONFLICT_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("lock wait timeout"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SQL"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(rawLockMessage))));

        verify(transactionService, times(1)).saleProduct(any(TransactionRequest.class));
    }

    @Test
    void unexpectedRuntimeFailureReturnsSafe500() throws Exception {
        TransactionRequest request = validSaleRequest();
        // A message of the kind an unmapped failure really carries: internal identifiers
        // and connection details that must stay in the log and out of the response.
        String rawMessage = "NullPointerException in com.ims.internal.Ledger at jdbc:mysql://db-primary:3306/stock";
        when(transactionService.saleProduct(any(TransactionRequest.class)))
                .thenThrow(new RuntimeException(rawMessage));

        mockMvc.perform(post("/api/transactions/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value(GENERIC_ERROR_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(rawMessage))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("jdbc"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("com.ims"))));
    }

    @Test
    void databaseIntegrityConflictReturnsSafe409() throws Exception {
        TransactionRequest request = validSaleRequest();
        String rawMessage = "could not execute statement; SQL [insert into stock_transactions ...]; "
                + "constraint [FK_stock_transactions_user_id]; nested exception is "
                + "org.hibernate.exception.ConstraintViolationException";
        when(transactionService.saleProduct(any(TransactionRequest.class)))
                .thenThrow(new DataIntegrityViolationException(rawMessage));

        String body = mockMvc.perform(post("/api/transactions/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value(DATA_CONFLICT_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn().getResponse().getContentAsString();

        String lowerBody = body.toLowerCase();
        for (String disclosure : java.util.List.of(
                "constraint", "foreign key", "sql", "jdbc", "hibernate", "products", "stock_transactions", "fk")) {
            org.junit.jupiter.api.Assertions.assertFalse(lowerBody.contains(disclosure),
                    "conflict response must not disclose '" + disclosure + "', body was: " + body);
        }
    }
}
