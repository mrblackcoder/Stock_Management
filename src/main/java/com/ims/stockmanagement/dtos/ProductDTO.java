package com.ims.stockmanagement.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private String imageUrl;
    private Long createdByUserId;
    private String createdByUsername;
    private LocalDateTime createdAt;
}

