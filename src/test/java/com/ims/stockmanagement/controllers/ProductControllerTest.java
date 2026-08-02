package com.ims.stockmanagement.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.stockmanagement.dtos.ProductDTO;
import com.ims.stockmanagement.dtos.ProductUpdateRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.GlobalExceptionHandler;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.exceptions.ProductHasTransactionHistoryException;
import com.ims.stockmanagement.services.ExternalApiService;
import com.ims.stockmanagement.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ExternalApiService externalApiService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductDTO validProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Laptop Dell XPS 15");
        dto.setSku("LAP-001");
        dto.setPrice(new BigDecimal("1500.00"));
        dto.setStockQuantity(50);
        dto.setReorderLevel(10);
        dto.setCategoryId(1L);
        return dto;
    }

    @Test
    void createProduct_returns201WithBody() throws Exception {
        ProductDTO request = validProductDTO();

        ProductDTO returned = new ProductDTO();
        returned.setId(1L);
        returned.setSku("LAP-001");
        Response serviceResponse = Response.builder()
                .statusCode(201)
                .message("Product created successfully")
                .product(returned)
                .build();
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.product.sku").value("LAP-001"));

        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    void getProductById_returns200WithProduct() throws Exception {
        ProductDTO returned = new ProductDTO();
        returned.setId(1L);
        returned.setSku("LAP-001");
        Response serviceResponse = Response.builder()
                .statusCode(200)
                .message("Product retrieved successfully")
                .product(returned)
                .build();
        when(productService.getProductById(1L)).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.product.id").value(1));

        verify(productService, times(1)).getProductById(1L);
    }

    private ProductUpdateRequest validUpdateRequest() {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Laptop Dell XPS 15");
        request.setSku("LAP-001");
        request.setDescription("Refreshed description");
        request.setPrice(new BigDecimal("1500.00"));
        request.setReorderLevel(10);
        request.setCategoryId(1L);
        return request;
    }

    @Test
    void updateProduct_withoutStockQuantityReturns200() throws Exception {
        ProductUpdateRequest request = validUpdateRequest();

        Response serviceResponse = Response.builder()
                .statusCode(200)
                .message("Product updated successfully")
                .build();
        when(productService.updateProduct(eq(1L), any(ProductUpdateRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));

        // The body binds to the update-only type, carrying every editable field.
        ArgumentCaptor<ProductUpdateRequest> bound = ArgumentCaptor.forClass(ProductUpdateRequest.class);
        verify(productService, times(1)).updateProduct(eq(1L), bound.capture());
        assertEquals("Laptop Dell XPS 15", bound.getValue().getName());
        assertEquals("LAP-001", bound.getValue().getSku());
        assertEquals("Refreshed description", bound.getValue().getDescription());
        assertEquals(new BigDecimal("1500.00"), bound.getValue().getPrice());
        assertEquals(10, bound.getValue().getReorderLevel());
        assertEquals(1L, bound.getValue().getCategoryId());
    }

    @Test
    void updateProduct_withStockQuantityReturns400AndDoesNotInvokeService() throws Exception {
        // Otherwise-valid edit that additionally carries an absolute stock value.
        String body = """
                {
                  "name": "Laptop Dell XPS 15",
                  "sku": "LAP-001",
                  "description": "Refreshed description",
                  "price": 1500.00,
                  "reorderLevel": 10,
                  "categoryId": 1,
                  "stockQuantity": 99
                }
                """;

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400));

        // Rejection happens during JSON binding, before the service is reached.
        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void createProduct_invalidBodyReturns400() throws Exception {
        // Violates multiple constraints: blank name, invalid/short SKU,
        // and null price / stockQuantity / categoryId (all @NotNull).
        ProductDTO invalid = new ProductDTO();
        invalid.setName("");
        invalid.setSku("!!");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(productService, never()).createProduct(any(ProductDTO.class));
    }

    @Test
    void getProductById_notFoundReturns404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new NotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message", containsString("not found")));

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void createProduct_duplicateSkuReturns409() throws Exception {
        ProductDTO request = validProductDTO();

        when(productService.createProduct(any(ProductDTO.class)))
                .thenThrow(new AlreadyExistsException("Product already exists with SKU: LAP-001"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message", containsString("already exists")));

        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    void deleteProduct_withTransactionHistoryReturns409() throws Exception {
        when(productService.deleteProduct(1L)).thenThrow(new ProductHasTransactionHistoryException(
                "Product cannot be deleted because it has stock transaction history."));

        String body = mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Product cannot be deleted because it has stock transaction history."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // The client never sees database internals behind the conflict.
        for (String leak : new String[]{"constraint", "foreign key", "SQL", "FK9", "stock_transactions", "database"}) {
            assertFalse(body.toLowerCase().contains(leak.toLowerCase()),
                    "response body must not disclose '" + leak + "': " + body);
        }

        verify(productService, times(1)).deleteProduct(1L);
    }
}
