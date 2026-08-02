package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.ProductDTO;
import com.ims.stockmanagement.dtos.ProductUpdateRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.exceptions.ProductHasTransactionHistoryException;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.Supplier;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.StockTransactionRepository;
import com.ims.stockmanagement.repositories.SupplierRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private Supplier testSupplier;
    private User testUser;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        // Setup test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        // Setup test supplier
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("TechSupply Inc");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@local");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop Dell XPS 15");
        testProduct.setSku("LAP-001");
        testProduct.setPrice(BigDecimal.valueOf(1500.00));
        testProduct.setStockQuantity(50);
        testProduct.setReorderLevel(10);
        testProduct.setCategory(testCategory);
        testProduct.setSupplier(testSupplier);
        testProduct.setCreatedBy(testUser);

        // Setup test DTO
        testProductDTO = new ProductDTO();
        testProductDTO.setId(1L);
        testProductDTO.setName("Laptop Dell XPS 15");
        testProductDTO.setSku("LAP-001");
        testProductDTO.setPrice(BigDecimal.valueOf(1500.00));
        testProductDTO.setStockQuantity(50);
        testProductDTO.setReorderLevel(10);
        testProductDTO.setCategoryId(1L);
        testProductDTO.setSupplierId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAllByOrderByCreatedAtDesc()).thenReturn(products);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.getAllProducts();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getProductList());
        assertEquals(1, response.getProductList().size());
        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testProduct));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.getProductById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getProduct());
        assertEquals("LAP-001", response.getProduct().getSku());
    }

    @Test
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findByIdWithRelations(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.getProductById(999L)
        );

        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void testCreateProduct_Success() {
        // Arrange
        setupSecurityContext();
        
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.createProduct(testProductDTO);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertEquals("Product created successfully", response.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_SKUAlreadyExists() {
        // Arrange
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(
            Exception.class,
            () -> productService.createProduct(testProductDTO)
        );

        assertTrue(exception.getMessage().contains("LAP-001") || exception.getMessage().contains("already exists"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testCreateProduct_CategoryNotFound() {
        // Arrange
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.createProduct(testProductDTO)
        );

        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    void testUpdateProduct_Success() {
        // Arrange
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated Laptop");
        updateRequest.setSku("LAP-001"); // unchanged, so no uniqueness lookup is triggered
        updateRequest.setPrice(BigDecimal.valueOf(1600.00));
        updateRequest.setCategoryId(1L);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.updateProduct(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Product updated successfully", response.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * Regression guard for the create path: introducing the update-only request type
     * must not stop ProductDTO from carrying an opening stock into a new product.
     */
    @Test
    void createProduct_persistsOpeningStockQuantity() {
        // Arrange
        setupSecurityContext();

        ProductDTO createDTO = new ProductDTO();
        createDTO.setName("Opening Stock Laptop");
        createDTO.setSku("LAP-OPEN-001");
        createDTO.setPrice(BigDecimal.valueOf(1200.00));
        createDTO.setStockQuantity(42);
        createDTO.setReorderLevel(7);
        createDTO.setCategoryId(1L);
        createDTO.setSupplierId(1L);

        when(productRepository.existsBySku("LAP-OPEN-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.createProduct(createDTO);

        // Assert
        assertEquals(201, response.getStatusCode());

        ArgumentCaptor<Product> persisted = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(persisted.capture());
        assertEquals(42, persisted.getValue().getStockQuantity());
        assertEquals(7, persisted.getValue().getReorderLevel());
        assertEquals(testUser, persisted.getValue().getCreatedBy());
    }

    /**
     * The update path must read the product through the pessimistic-lock query and must
     * never assign stock. Hibernate emits a full-column UPDATE, so the only thing keeping
     * stock correct is that the value written back is the one just read under the lock.
     */
    @Test
    void updateProduct_loadsUnderLockAndNeverAssignsStock() {
        // Arrange
        Product lockedProduct = new Product();
        lockedProduct.setId(1L);
        lockedProduct.setName("Laptop Dell XPS 15");
        lockedProduct.setSku("LAP-001");
        lockedProduct.setPrice(BigDecimal.valueOf(1500.00));
        lockedProduct.setStockQuantity(50);
        lockedProduct.setReorderLevel(10);
        lockedProduct.setCategory(testCategory);

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Renamed Laptop");
        updateRequest.setSku("LAP-002"); // changed, so the uniqueness check runs
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(BigDecimal.valueOf(1750.00));
        updateRequest.setReorderLevel(25);
        updateRequest.setCategoryId(1L);
        updateRequest.setSupplierId(1L);
        updateRequest.setImageUrl("https://example.com/laptop.png");

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lockedProduct));
        when(productRepository.existsBySku("LAP-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(productRepository.save(any(Product.class))).thenReturn(lockedProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.updateProduct(1L, updateRequest);

        // Assert
        assertEquals(200, response.getStatusCode());

        // The lookup is the locking one, never the plain fetch-join read.
        verify(productRepository).findByIdForUpdate(1L);
        verify(productRepository, never()).findByIdWithRelations(anyLong());

        ArgumentCaptor<Product> persisted = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(persisted.capture());
        Product saved = persisted.getValue();

        // Stock is exactly what the locked read returned.
        assertEquals(50, saved.getStockQuantity());

        // Every editable field was applied.
        assertEquals("Renamed Laptop", saved.getName());
        assertEquals("LAP-002", saved.getSku());
        assertEquals("Updated description", saved.getDescription());
        assertEquals(BigDecimal.valueOf(1750.00), saved.getPrice());
        assertEquals(25, saved.getReorderLevel());
        assertEquals(testCategory, saved.getCategory());
        assertEquals(testSupplier, saved.getSupplier());
        assertEquals("https://example.com/laptop.png", saved.getImageUrl());

        // No stock-owning collaborator participates in an ordinary edit.
        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteProduct_withoutHistorySucceeds() {
        // Arrange
        setupSecurityContext();
        doReturn(java.util.Collections.singletonList(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")))
            .when(authentication).getAuthorities();

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(stockTransactionRepository.existsByProductId(1L)).thenReturn(false);
        doNothing().when(productRepository).delete(testProduct);

        // Act
        Response response = productService.deleteProduct(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Product deleted successfully", response.getMessage());

        // The deletion lookup is the locking one, never the plain fetch-join read.
        verify(productRepository, times(1)).findByIdForUpdate(1L);
        verify(productRepository, never()).findByIdWithRelations(anyLong());
        verify(productRepository, times(1)).delete(testProduct);
    }

    /**
     * Ledger history is audit data: a product carrying transactions cannot be hard
     * deleted, and ADMIN is no exception.
     */
    @Test
    void deleteProduct_withHistoryIsRejected() {
        // Arrange - caller is ADMIN, the strongest role available.
        setupSecurityContext();
        doReturn(java.util.Collections.singletonList(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")))
            .when(authentication).getAuthorities();

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(stockTransactionRepository.existsByProductId(1L)).thenReturn(true);

        // Act
        ProductHasTransactionHistoryException exception = assertThrows(
                ProductHasTransactionHistoryException.class,
                () -> productService.deleteProduct(1L));

        // Assert
        assertEquals("Product cannot be deleted because it has stock transaction history.",
                exception.getMessage());

        // Neither the product nor any ledger row is touched: the existence probe is the
        // only interaction with the transaction repository.
        verify(productRepository, never()).delete(any(Product.class));
        verify(stockTransactionRepository, times(1)).existsByProductId(1L);
        verifyNoMoreInteractions(stockTransactionRepository);
    }

    @Test
    void testGetLowStockProducts_Success() {
        // Arrange
        Product lowStockProduct = new Product();
        lowStockProduct.setId(2L);
        lowStockProduct.setName("Low Stock Item");
        lowStockProduct.setStockQuantity(5);
        lowStockProduct.setReorderLevel(10);

        when(productRepository.findLowStockProducts()).thenReturn(Arrays.asList(lowStockProduct));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.getLowStockProducts();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getMessage().contains("Low stock"));
        verify(productRepository, times(1)).findLowStockProducts();
    }

    @Test
    void testSearchProducts_Success() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.findByNameOrSkuContainingWithRelations(keyword))
            .thenReturn(Arrays.asList(testProduct));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.searchProducts(keyword);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getProductList());
        assertEquals(1, response.getProductList().size());
    }
}
