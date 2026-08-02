package com.ims.stockmanagement.exceptions;

public class ProductHasTransactionHistoryException extends RuntimeException {
    public ProductHasTransactionHistoryException(String message) {
        super(message);
    }
}
