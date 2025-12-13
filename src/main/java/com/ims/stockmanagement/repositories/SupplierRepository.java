package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Supplier> findByNameContainingIgnoreCase(String name);

    // N+1 optimization: Fetch products when checking for delete
    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.products WHERE s.id = :id")
    Optional<Supplier> findByIdWithProducts(@Param("id") Long id);
}
