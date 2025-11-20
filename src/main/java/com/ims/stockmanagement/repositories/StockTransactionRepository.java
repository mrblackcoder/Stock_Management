package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProduct(Product product);

    List<StockTransaction> findByUser(User user);

    List<StockTransaction> findByTransactionType(TransactionType transactionType);

    List<StockTransaction> findByStatus(TransactionStatus status);

    List<StockTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<StockTransaction> findByProductIdOrderByTransactionDateDesc(Long productId);
}

