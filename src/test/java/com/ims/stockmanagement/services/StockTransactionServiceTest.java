package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionDTO;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.exceptions.InsufficientStockException;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.StockTransactionRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockTransactionServiceTest {

    @Mock
    private StockTransactionRepository transactionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StockTransactionService stockTransactionService;

    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop Dell XPS 15");
        testProduct.setSku("LAP-001");
        testProduct.setPrice(BigDecimal.valueOf(1500.00));
        testProduct.setStockQuantity(50);
        testProduct.setReorderLevel(10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void purchaseIncreasesStockAndSavesTransaction() {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(1L);
        request.setUserId(1L);
        request.setTransactionType(TransactionType.PURCHASE);
        request.setQuantity(20);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(StockTransaction.class), eq(TransactionDTO.class))).thenReturn(new TransactionDTO());

        Response response = stockTransactionService.createTransaction(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        assertEquals(70, productCaptor.getValue().getStockQuantity().intValue());

        ArgumentCaptor<StockTransaction> txnCaptor = ArgumentCaptor.forClass(StockTransaction.class);
        verify(transactionRepository, times(1)).save(txnCaptor.capture());
        StockTransaction saved = txnCaptor.getValue();
        assertEquals(TransactionType.PURCHASE, saved.getTransactionType());
        assertEquals(20, saved.getQuantity().intValue());
        assertSame(testProduct, saved.getProduct());
        assertSame(testUser, saved.getUser());

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getTransaction());
    }

    @Test
    void saleDecreasesStockAndSavesTransaction() {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(1L);
        request.setUserId(1L);
        request.setTransactionType(TransactionType.SALE);
        request.setQuantity(30);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(StockTransaction.class), eq(TransactionDTO.class))).thenReturn(new TransactionDTO());

        Response response = stockTransactionService.createTransaction(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        assertEquals(20, productCaptor.getValue().getStockQuantity().intValue());

        ArgumentCaptor<StockTransaction> txnCaptor = ArgumentCaptor.forClass(StockTransaction.class);
        verify(transactionRepository, times(1)).save(txnCaptor.capture());
        StockTransaction saved = txnCaptor.getValue();
        assertEquals(TransactionType.SALE, saved.getTransactionType());
        assertEquals(30, saved.getQuantity().intValue());
        assertSame(testProduct, saved.getProduct());
        assertSame(testUser, saved.getUser());

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getTransaction());
    }

    @Test
    void saleWithInsufficientStockThrowsAndPersistsNothing() {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(1L);
        request.setUserId(1L);
        request.setTransactionType(TransactionType.SALE);
        request.setQuantity(100); // greater than the 50 in stock

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> stockTransactionService.createTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertTrue(exception.getMessage().contains(testProduct.getName()));

        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(StockTransaction.class));
        verifyNoInteractions(modelMapper);
    }

    @Test
    void actorResolvedFromSecurityContextWhenUserIdNull() {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(1L);
        request.setUserId(null);
        request.setTransactionType(TransactionType.PURCHASE);
        request.setQuantity(10);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(StockTransaction.class), eq(TransactionDTO.class))).thenReturn(new TransactionDTO());

        Response response = stockTransactionService.createTransaction(request);

        ArgumentCaptor<StockTransaction> txnCaptor = ArgumentCaptor.forClass(StockTransaction.class);
        verify(transactionRepository, times(1)).save(txnCaptor.capture());
        assertSame(testUser, txnCaptor.getValue().getUser());

        verify(userRepository, times(1)).findByUsername("admin");
        verify(userRepository, never()).findById(anyLong());
        assertEquals(201, response.getStatusCode());
    }
}
