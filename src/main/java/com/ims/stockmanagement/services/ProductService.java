package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.ProductDTO;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.Supplier;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    /**
     * Tüm ürünleri listele (READ - CRUD)
     */
    public Response getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Products retrieved successfully")
                .productList(productDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tüm ürünleri sayfalı listele
     */
    public Response getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Products retrieved successfully")
                .productList(productDTOs)
                .data(productPage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Ürün arama metodu
     */
    public Response searchProducts(String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(keyword, keyword);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Products found: " + productDTOs.size())
                .productList(productDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ID'ye göre ürün getir (READ - CRUD)
     */
    public Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        ProductDTO productDTO = convertToDTO(product);

        return Response.builder()
                .statusCode(200)
                .message("Product retrieved successfully")
                .product(productDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Yeni ürün oluştur (CREATE - CRUD)
     */
    @Transactional
    public Response createProduct(ProductDTO productDTO) {
        // SKU kontrolü
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new AlreadyExistsException("Product already exists with SKU: " + productDTO.getSku());
        }

        // Kategori kontrolü
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        // Tedarikçi kontrolü (opsiyonel)
        Supplier supplier = null;
        if (productDTO.getSupplierId() != null) {
            supplier = supplierRepository.findById(productDTO.getSupplierId())
                    .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + productDTO.getSupplierId()));
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setSku(productDTO.getSku());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setReorderLevel(productDTO.getReorderLevel());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setImageUrl(productDTO.getImageUrl());

        Product savedProduct = productRepository.save(product);
        ProductDTO savedProductDTO = convertToDTO(savedProduct);

        return Response.builder()
                .statusCode(201)
                .message("Product created successfully")
                .product(savedProductDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Ürün güncelle (UPDATE - CRUD)
     */
    @Transactional
    public Response updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        // SKU değiştiyse ve başka biri kullanıyorsa hata ver
        if (!existingProduct.getSku().equals(productDTO.getSku()) &&
                productRepository.existsBySku(productDTO.getSku())) {
            throw new AlreadyExistsException("Product already exists with SKU: " + productDTO.getSku());
        }

        // Kategori kontrolü
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        // Tedarikçi kontrolü (opsiyonel)
        Supplier supplier = null;
        if (productDTO.getSupplierId() != null) {
            supplier = supplierRepository.findById(productDTO.getSupplierId())
                    .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + productDTO.getSupplierId()));
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setSku(productDTO.getSku());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());
        existingProduct.setReorderLevel(productDTO.getReorderLevel());
        existingProduct.setCategory(category);
        existingProduct.setSupplier(supplier);
        existingProduct.setImageUrl(productDTO.getImageUrl());

        Product updatedProduct = productRepository.save(existingProduct);
        ProductDTO updatedProductDTO = convertToDTO(updatedProduct);

        return Response.builder()
                .statusCode(200)
                .message("Product updated successfully")
                .product(updatedProductDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Ürün sil (DELETE - CRUD)
     */
    @Transactional
    public Response deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        productRepository.delete(product);

        return Response.builder()
                .statusCode(200)
                .message("Product deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * İsme göre ürün ara
     */
    public Response searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Products found: " + productDTOs.size())
                .productList(productDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Kategoriye göre ürünleri getir
     */
    public Response getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));

        List<Product> products = productRepository.findByCategory(category);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Products found: " + productDTOs.size())
                .productList(productDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Düşük stoklu ürünleri getir
     */
    public Response getLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Low stock products found: " + productDTOs.size())
                .productList(productDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Product entity'yi DTO'ya dönüştür
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        if (product.getSupplier() != null) {
            dto.setSupplierId(product.getSupplier().getId());
            dto.setSupplierName(product.getSupplier().getName());
        }
        return dto;
    }
}

