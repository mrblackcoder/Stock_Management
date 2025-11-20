package com.ims.stockmanagement.dtos;

import com.ims.stockmanagement.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private Long productId;
    private Long userId;
    private TransactionType transactionType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String notes;
}

