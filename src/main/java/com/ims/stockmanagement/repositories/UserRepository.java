package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /**
     * Case-insensitive counterpart to findByEmail.
     *
     * Uniqueness of email is ultimately enforced by the database index, whose
     * case-sensitivity depends on the column collation (MySQL's default is
     * case-insensitive, H2's is not). Comparing in the application makes the
     * duplicate rule behave the same way on both.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    long countByRole(UserRole role);
}


