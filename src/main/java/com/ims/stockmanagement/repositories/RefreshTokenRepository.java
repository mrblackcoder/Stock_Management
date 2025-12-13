package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.models.RefreshToken;
import com.ims.stockmanagement.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUserAndRevokedFalse(User user);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
    void deleteAllUserTokens(User user);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
