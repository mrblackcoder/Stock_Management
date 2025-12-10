package com.ims.stockmanagement.dtos;

import com.ims.stockmanagement.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long userId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Unit price format is invalid")
    private BigDecimal unitPrice;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
