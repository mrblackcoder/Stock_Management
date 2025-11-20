package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.services.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockTransactionController {

    private final StockTransactionService transactionService;

    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Response> purchaseProduct(@RequestBody TransactionRequest request) {
        Response response = transactionService.purchaseProduct(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Response> saleProduct(@RequestBody TransactionRequest request) {
        Response response = transactionService.saleProduct(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Response response = transactionService.getAllTransactions(pageable);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getTransactionById(@PathVariable Long id) {
        Response response = transactionService.getTransactionById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Response> getTransactionsByProduct(@PathVariable Long productId) {
        Response response = transactionService.getTransactionsByProduct(productId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getTransactionsByUser(@PathVariable Long userId) {
        Response response = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Response response = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

