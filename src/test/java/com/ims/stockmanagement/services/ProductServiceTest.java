package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dto.ProductDTO;
import com.ims.stockmanagement.dto.Response;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.Supplier;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.SupplierRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        testProductDTO.setName("Laptop Dell XPS 15");
        testProductDTO.setSku("LAP-001");
        testProductDTO.setPrice(BigDecimal.valueOf(1500.00));
        testProductDTO.setStockQuantity(50);
        testProductDTO.setReorderLevel(10);
        testProductDTO.setCategoryId(1L);
        testProductDTO.setSupplierId(1L);

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin");
    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findAllWithSupplierAndCategory()).thenReturn(products);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Products retrieved successfully", response.getMessage());
        assertNotNull(response.getProductList());
        assertEquals(1, response.getProductList().size());
        verify(productRepository, times(1)).findAllWithSupplierAndCategory();
    }

    @Test
    void testCreateProduct_Success() {
        // Arrange
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.createProduct(testProductDTO);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertEquals("Product created successfully", response.getMessage());
        assertNotNull(response.getProduct());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_SKUAlreadyExists() {
        // Arrange
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(testProductDTO)
        );

        assertEquals("Product with SKU LAP-001 already exists", exception.getMessage());
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

        assertEquals("Category not found with id: 1", exception.getMessage());
    }

    @Test
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
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
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.getProductById(999L)
        );

        assertEquals("Product not found with id: 999", exception.getMessage());
    }

    @Test
    void testUpdateProduct_Success() {
        // Arrange
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setName("Updated Laptop");
        updateDTO.setPrice(BigDecimal.valueOf(1600.00));

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(updateDTO);

        // Act
        Response response = productService.updateProduct(1L, updateDTO);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Product updated successfully", response.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success_AsOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(authentication.getName()).thenReturn("admin");

        // Act
        Response response = productService.deleteProduct(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Product deleted successfully", response.getMessage());
        verify(productRepository, times(1)).delete(testProduct);
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

        // Act
        Response response = productService.getLowStockProducts();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getMessage().contains("low stock"));
        verify(productRepository, times(1)).findLowStockProducts();
    }

    @Test
    void testSearchProducts_Success() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.findByNameContainingIgnoreCase(keyword))
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

    @Test
    void testGetProductBySku_Success() {
        // Arrange
        when(productRepository.findBySku("LAP-001")).thenReturn(Optional.of(testProduct));
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(testProductDTO);

        // Act
        Response response = productService.getProductBySku("LAP-001");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getProduct());
        assertEquals("LAP-001", response.getProduct().getSku());
    }

    @Test
    void testGetProductBySku_NotFound() {
        // Arrange
        when(productRepository.findBySku("INVALID-SKU")).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.getProductBySku("INVALID-SKU")
        );

        assertEquals("Product not found with SKU: INVALID-SKU", exception.getMessage());
    }
}
