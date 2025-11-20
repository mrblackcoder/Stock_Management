package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);

    List<Product> findByCategory(Category category);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.reorderLevel")
    List<Product> findLowStockProducts();

    boolean existsBySku(String sku);

    // Son eklenen ürünleri getir (createdAt'e göre azalan sıralı)
    List<Product> findAllByOrderByCreatedAtDesc();

    // Pageable ile limitli sorgulama için
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(Pageable pageable);
}

