package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionDTO;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.TransactionStatus;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.exceptions.InsufficientStockException;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.StockTransactionRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockTransactionService {

    private final StockTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Tüm işlemleri listele (READ - CRUD)
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getAllTransactions() {
        List<StockTransaction> transactions = transactionRepository.findAllWithProductAndUser();
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Transactions retrieved successfully")
                .transactionList(transactionDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tüm işlemleri sayfalı listele
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getAllTransactions(Pageable pageable) {
        Page<StockTransaction> transactionPage = transactionRepository.findAllWithProductAndUser(pageable);
        List<TransactionDTO> transactionDTOs = transactionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Transactions retrieved successfully")
                .transactionList(transactionDTOs)
                .data(transactionPage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Ürün satın alma (PURCHASE)
     */
    @Transactional
    public Response purchaseProduct(TransactionRequest request) {
        request.setTransactionType(TransactionType.PURCHASE);
        return createTransaction(request);
    }

    /**
     * Ürün satışı (SALE)
     */
    @Transactional
    public Response saleProduct(TransactionRequest request) {
        request.setTransactionType(TransactionType.SALE);
        return createTransaction(request);
    }

    /**
     * Stok düzeltme (ADJUSTMENT)
     */
    @Transactional
    public Response adjustStock(TransactionRequest request) {
        request.setTransactionType(TransactionType.ADJUSTMENT);
        return createTransaction(request);
    }

    /**
     * İşlem durumunu güncelle
     */
    @Transactional
    public Response updateTransactionStatus(Long id, String statusStr) {
        StockTransaction transaction = transactionRepository.findByIdWithProductAndUser(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        try {
            TransactionStatus newStatus = TransactionStatus.valueOf(statusStr.toUpperCase());
            transaction.setStatus(newStatus);
            StockTransaction updatedTransaction = transactionRepository.save(transaction);
            TransactionDTO transactionDTO = convertToDTO(updatedTransaction);

            return Response.builder()
                    .statusCode(200)
                    .message("Transaction status updated successfully")
                    .transaction(transactionDTO)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.builder()
                    .statusCode(400)
                    .message("Invalid transaction status: " + statusStr)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * ID'ye göre işlem getir (READ - CRUD)
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getTransactionById(Long id) {
        StockTransaction transaction = transactionRepository.findByIdWithProductAndUser(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        TransactionDTO transactionDTO = convertToDTO(transaction);

        return Response.builder()
                .statusCode(200)
                .message("Transaction retrieved successfully")
                .transaction(transactionDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Yeni stok işlemi oluştur (CREATE - CRUD)
     * PURCHASE: Alım (Stok artar)
     * SALE: Satış (Stok azalır)
     * ADJUSTMENT: Düzeltme (Stok artar veya azalır)
     */
    @Transactional
    public Response createTransaction(TransactionRequest request) {
        // Ürün kontrolü
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + request.getProductId()));

        // Kullanıcı kontrolü - request'ten gelmiyorsa authenticated user'ı al
        User user;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getUserId()));
        } else {
            // Spring Security context'inden authenticated user'ı al
            org.springframework.security.core.Authentication authentication =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new SecurityException("No authenticated user found. Please login first.");
            }
            String username = authentication.getName();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        }

        // İşlem tipine göre stok kontrolü
        if (request.getTransactionType() == TransactionType.SALE) {
            if (product.getStockQuantity() < request.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() +
                        ", Requested: " + request.getQuantity()
                );
            }
        }

        // Transaction oluştur
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setUser(user);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setQuantity(request.getQuantity());
        transaction.setUnitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : product.getPrice());
        transaction.setTotalPrice(transaction.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setNotes(request.getNotes());

        // Stok güncelle
        updateProductStock(product, request.getTransactionType(), request.getQuantity());

        StockTransaction savedTransaction = transactionRepository.save(transaction);
        TransactionDTO transactionDTO = convertToDTO(savedTransaction);

        return Response.builder()
                .statusCode(201)
                .message("Transaction created successfully")
                .transaction(transactionDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * İşlem sil (DELETE - CRUD)
     * Not: Stok işlemi silme genelde önerilmez, ancak CRUD için eklendi
     */
    @Transactional
    public Response deleteTransaction(Long id) {
        StockTransaction transaction = transactionRepository.findByIdWithProductAndUser(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        // Stoku geri al (reverse operation)
        Product product = transaction.getProduct();
        TransactionType reverseType;

        switch (transaction.getTransactionType()) {
            case PURCHASE:
                reverseType = TransactionType.SALE;
                break;
            case SALE:
                reverseType = TransactionType.PURCHASE;
                break;
            default:
                reverseType = TransactionType.ADJUSTMENT;
        }

        updateProductStock(product, reverseType, transaction.getQuantity());

        transactionRepository.delete(transaction);

        return Response.builder()
                .statusCode(200)
                .message("Transaction deleted successfully and stock reverted")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Ürüne göre işlemleri getir
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getTransactionsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<StockTransaction> transactions = transactionRepository.findByProductWithRelations(product);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Transactions found: " + transactionDTOs.size())
                .transactionList(transactionDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Kullanıcıya göre işlemleri getir
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getTransactionsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<StockTransaction> transactions = transactionRepository.findByUserWithRelations(user);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Transactions found: " + transactionDTOs.size())
                .transactionList(transactionDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Tarih aralığına göre işlemleri getir
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<StockTransaction> transactions = transactionRepository.findByTransactionDateBetweenWithRelations(startDate, endDate);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Transactions found: " + transactionDTOs.size())
                .transactionList(transactionDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * İşlem tipine göre işlemleri getir
     * N+1 optimized: Uses FETCH JOIN to load product and user in single query
     */
    public Response getTransactionsByType(TransactionType type) {
        List<StockTransaction> transactions = transactionRepository.findByTransactionTypeWithRelations(type);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Transactions found: " + transactionDTOs.size())
                .transactionList(transactionDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * İşlem güncelle (UPDATE - CRUD)
     * Not: Sadece notes ve status güncellenebilir, stok değişikliği için yeni işlem oluşturulmalı
     */
    @Transactional
    public Response updateTransaction(Long id, TransactionRequest request) {
        StockTransaction transaction = transactionRepository.findByIdWithProductAndUser(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));

        // Sadece notes güncellenebilir (stok tutarlılığı için diğer alanlar değiştirilemez)
        if (request.getNotes() != null) {
            transaction.setNotes(request.getNotes());
        }

        StockTransaction updatedTransaction = transactionRepository.save(transaction);
        TransactionDTO transactionDTO = convertToDTO(updatedTransaction);

        return Response.builder()
                .statusCode(200)
                .message("Transaction updated successfully")
                .transaction(transactionDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Ürün stok güncellemesi
     */
    private void updateProductStock(Product product, TransactionType type, Integer quantity) {
        switch (type) {
            case PURCHASE:
            case ADJUSTMENT:
                product.setStockQuantity(product.getStockQuantity() + quantity);
                break;
            case SALE:
                product.setStockQuantity(product.getStockQuantity() - quantity);
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + type);
        }
        productRepository.save(product);
    }

    /**
     * StockTransaction entity'yi DTO'ya dönüştür
     */
    private TransactionDTO convertToDTO(StockTransaction transaction) {
        TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);

        // Null safety for product
        if (transaction.getProduct() != null) {
            dto.setProductId(transaction.getProduct().getId());
            dto.setProductName(transaction.getProduct().getName());
            dto.setProductSku(transaction.getProduct().getSku());
        }

        // Null safety for user
        if (transaction.getUser() != null) {
            dto.setUserId(transaction.getUser().getId());
            dto.setUsername(transaction.getUser().getUsername());
        }

        return dto;
    }
}

