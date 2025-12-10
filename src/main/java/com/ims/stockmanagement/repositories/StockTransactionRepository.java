package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    // N+1 optimization: Fetch product and user in single query
    @Query("SELECT DISTINCT t FROM StockTransaction t LEFT JOIN FETCH t.product LEFT JOIN FETCH t.user")
    List<StockTransaction> findAllWithProductAndUser();

    // Pageable with fetch join
    @Query(value = "SELECT DISTINCT t FROM StockTransaction t LEFT JOIN FETCH t.product LEFT JOIN FETCH t.user",
           countQuery = "SELECT COUNT(t) FROM StockTransaction t")
    Page<StockTransaction> findAllWithProductAndUser(Pageable pageable);

    // Find by ID with relations
    @Query("SELECT t FROM StockTransaction t LEFT JOIN FETCH t.product LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<StockTransaction> findByIdWithProductAndUser(@Param("id") Long id);

    @Query("SELECT t FROM StockTransaction t LEFT JOIN FETCH t.product LEFT JOIN FETCH t.user WHERE t.product = :product")
    List<StockTransaction> findByProductWithRelations(@Param("product") Product product);

    @Query("SELECT t FROM StockTransaction t LEFT JOIN FETCH t.product LEFT JOIN FETCH t.user WHERE t.user = :user")
    List<StockTransaction> findByUserWithRelations(@Param("user") User user);

    @Query("SELECT t FROM StockTransaction t LEFT JOIN FETCH t.product LEFT JOIN FETCH t.user WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    List<StockTransaction> findByTransactionDateBetweenWithRelations(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Legacy methods (kept for backward compatibility)
    List<StockTransaction> findByProduct(Product product);

    List<StockTransaction> findByUser(User user);

    List<StockTransaction> findByTransactionType(TransactionType transactionType);

    List<StockTransaction> findByStatus(TransactionStatus status);

    List<StockTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<StockTransaction> findByProductIdOrderByTransactionDateDesc(Long productId);
}

