package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.ProductDTO;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.services.ExternalApiService;
import com.ims.stockmanagement.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@org.springframework.validation.annotation.Validated
public class ProductController {

    private final ProductService productService;
    private final ExternalApiService externalApiService;

    // Allowed sort fields whitelist
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id", "name", "sku", "price", "stockQuantity", "createdAt"
    );

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Response response = productService.createProduct(productDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        // Validate sortBy field to prevent injection
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, safeSortBy));
        Response response = productService.getAllProducts(pageable);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getProductById(@PathVariable Long id) {
        Response response = productService.getProductById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> searchProducts(
            @RequestParam @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Invalid search keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Response.builder()
                    .statusCode(400)
                    .message("Search keyword cannot be empty")
                    .timestamp(LocalDateTime.now())
                    .build());
        }
        Response response = productService.searchProducts(keyword.trim());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getProductsByCategory(@PathVariable Long categoryId) {
        Response response = productService.getProductsByCategory(categoryId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getLowStockProducts() {
        Response response = productService.getLowStockProducts();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        Response response = productService.updateProduct(id, productDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> deleteProduct(@PathVariable Long id) {
        Response response = productService.deleteProduct(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}/price/{currency}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getProductPriceInCurrency(
            @PathVariable Long id,
            @PathVariable @Pattern(regexp = "^[A-Za-z]{3}$", message = "Invalid currency code") String currency) {

        Response productResponse = productService.getProductById(id);
        ProductDTO product = productResponse.getProduct();

        if (product == null) {
            return ResponseEntity.status(404).body(Response.builder()
                    .statusCode(404)
                    .message("Product not found")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        try {
            BigDecimal originalPrice = product.getPrice();
            BigDecimal convertedPrice = externalApiService.convertPrice(
                    originalPrice,
                    "TRY",
                    currency.toUpperCase()
            );

            Response response = Response.builder()
                    .statusCode(200)
                    .message("Price converted successfully")
                    .data(Map.of(
                            "productId", product.getId(),
                            "productName", product.getName(),
                            "originalPrice", originalPrice,
                            "originalCurrency", "TRY",
                            "convertedPrice", convertedPrice,
                            "targetCurrency", currency.toUpperCase()
                    ))
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Response.builder()
                    .statusCode(500)
                    .message("Currency conversion failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/currencies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getSupportedCurrencies() {
        try {
            Map<String, Object> currencies = externalApiService.getSupportedCurrencies();

            Response response = Response.builder()
                    .statusCode(200)
                    .message("Supported currencies retrieved successfully")
                    .data(currencies)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Response.builder()
                    .statusCode(500)
                    .message("Failed to fetch currencies: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/exchange-rates")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getExchangeRates(
            @RequestParam(defaultValue = "USD") @Pattern(regexp = "^[A-Za-z]{3}$", message = "Invalid currency code") String base) {
        try {
            Map<String, Object> rates = externalApiService.getExchangeRates(base.toUpperCase());

            Response response = Response.builder()
                    .statusCode(200)
                    .message("Exchange rates retrieved successfully")
                    .data(rates)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Response.builder()
                    .statusCode(500)
                    .message("Failed to fetch exchange rates: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}
