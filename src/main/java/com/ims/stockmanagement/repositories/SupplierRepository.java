package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Supplier> findByNameContainingIgnoreCase(String name);
}
