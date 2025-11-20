package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);

    List<Product> findByCategory(Category category);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.category WHERE p.stockQuantity <= p.reorderLevel")
    List<Product> findLowStockProducts();

    boolean existsBySku(String sku);

    // Son eklenen ürünleri getir (createdAt'e göre azalan sıralı)
    // FETCH JOIN ile Supplier ve Category bilgilerini de yükle
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.category ORDER BY p.createdAt DESC")
    List<Product> findAllByOrderByCreatedAtDesc();

    // Pageable ile limitli sorgulama için
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.category ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(Pageable pageable);

    // Tüm ürünleri supplier ve category bilgileriyle birlikte getir
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.category")
    List<Product> findAllWithSupplierAndCategory();

    // Pageable sorgularda da FETCH JOIN kullan
    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.category",
           countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithRelations(Pageable pageable);
}

