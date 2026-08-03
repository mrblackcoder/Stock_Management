package com.ims.stockmanagement.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;
    private String token;
    private String refreshToken;
    private String role;
    private String expirationTime;
    private Object data;
    private LocalDateTime timestamp;

    // Pagination metadata for list endpoints. Explicit top-level fields rather than a
    // serialized Page: PageImpl's JSON shape is not a supported contract, and putting
    // the Page in `data` also exposed the raw entities it wraps.
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;

    // Specific response fields for different entities
    private UserDTO user;
    private CategoryDTO category;
    private ProductDTO product;
    private SupplierDTO supplier;
    private TransactionDTO transaction;
    private List<UserDTO> userList;
    private List<CategoryDTO> categoryList;
    private List<ProductDTO> productList;
    private List<SupplierDTO> supplierList;
    private List<TransactionDTO> transactionList;

    public Response(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Response(int statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}

