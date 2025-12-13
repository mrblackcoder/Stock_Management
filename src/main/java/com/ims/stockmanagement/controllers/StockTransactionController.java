package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.TransactionStatus;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.services.StockTransactionService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class StockTransactionController {

    private final StockTransactionService transactionService;

    // Allowed sort fields whitelist
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id", "transactionDate", "quantity", "totalAmount", "transactionType"
    );

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Response response;
        if (request.getTransactionType() == null) {
            response = Response.builder()
                    .statusCode(400)
                    .message("Transaction type is required")
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            switch (request.getTransactionType()) {
                case PURCHASE:
                    response = transactionService.purchaseProduct(request);
                    break;
                case SALE:
                    response = transactionService.saleProduct(request);
                    break;
                case ADJUSTMENT:
                    response = transactionService.adjustStock(request);
                    break;
                default:
                    response = Response.builder()
                            .statusCode(400)
                            .message("Invalid transaction type: " + request.getTransactionType())
                            .timestamp(LocalDateTime.now())
                            .build();
            }
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> purchaseProduct(@Valid @RequestBody TransactionRequest request) {
        Response response = transactionService.purchaseProduct(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/sale")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> saleProduct(@Valid @RequestBody TransactionRequest request) {
        Response response = transactionService.saleProduct(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getAllTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy) {

        // Validate sortBy field to prevent injection
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "transactionDate";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, safeSortBy));
        Response response = transactionService.getAllTransactions(pageable);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getTransactionById(@PathVariable Long id) {
        Response response = transactionService.getTransactionById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getTransactionsByProduct(@PathVariable Long productId) {
        Response response = transactionService.getTransactionsByProduct(productId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getTransactionsByUser(@PathVariable Long userId) {
        Response response = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/by-type/{type}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getTransactionsByType(
            @PathVariable @Pattern(regexp = "^(PURCHASE|SALE|ADJUSTMENT)$", message = "Invalid type. Must be PURCHASE, SALE, or ADJUSTMENT") String type) {
        try {
            TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
            Response response = transactionService.getTransactionsByType(transactionType);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (IllegalArgumentException e) {
            Response response = Response.builder()
                    .statusCode(400)
                    .message("Invalid transaction type: " + type)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        Response response = transactionService.updateTransaction(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam @Pattern(regexp = "^(PENDING|COMPLETED|CANCELLED)$", message = "Invalid status. Must be PENDING, COMPLETED, or CANCELLED") String status) {
        Response response = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteTransaction(@PathVariable Long id) {
        Response response = transactionService.deleteTransaction(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
