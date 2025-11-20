package com.ims.stockmanagement.dtos;

import com.ims.stockmanagement.enums.TransactionStatus;
import com.ims.stockmanagement.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long userId;
    private String username;
    private TransactionType transactionType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private TransactionStatus status;
    private String notes;
    private LocalDateTime transactionDate;
}

