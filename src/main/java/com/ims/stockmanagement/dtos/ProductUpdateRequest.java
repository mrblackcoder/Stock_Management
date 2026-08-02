package com.ims.stockmanagement.dtos;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request body for PUT /api/products/{id}.
 *
 * Deliberately carries no stockQuantity: after creation, stock is owned by the
 * stock-transaction ledger, so an ordinary product edit must never be able to
 * assign an absolute stock value. ProductDTO stays in use for creation (where an
 * opening stock is legitimate) and for responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "SKU can only contain alphanumeric characters, hyphens, and underscores")
    private String sku;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long supplierId;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    /**
     * Stock is derived from the stock-transaction ledger, never from an edit payload.
     * This local rejection stops a stale or malicious absolute value from reaching
     * ProductService: binding fails first, so the request is refused with HTTP 400.
     */
    @JsonSetter("stockQuantity")
    public void rejectClientSuppliedStockQuantity(Integer ignoredStockQuantity) {
        throw new IllegalArgumentException(
                "stockQuantity must not be supplied; use the stock transaction endpoints to change stock");
    }
}
