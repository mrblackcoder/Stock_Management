package com.ims.stockmanagement.dtos;

import com.fasterxml.jackson.annotation.JsonSetter;
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

    /**
     * The transaction actor is always derived from the authenticated principal.
     * This local rejection prevents older clients from silently supplying an identity.
     */
    @JsonSetter("userId")
    public void rejectClientSuppliedUserId(Long ignoredUserId) {
        throw new IllegalArgumentException("userId must not be supplied; transaction actor is derived from authentication");
    }
}
