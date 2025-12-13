package com.ims.stockmanagement.services;

import com.ims.stockmanagement.exceptions.InvalidCredentialsException;
import com.ims.stockmanagement.models.RefreshToken;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.RefreshTokenRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    @Value("${jwt.refresh.expiration:604800000}") // 7 days default
    private Long refreshTokenDurationMs;
    
    /**
     * Kullanıcı için yeni refresh token oluştur
     */
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + username));
        
        // Mevcut tokenları iptal et
        refreshTokenRepository.deleteAllUserTokens(user);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Refresh token'ı doğrula
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Token'ın geçerli olup olmadığını kontrol et
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new InvalidCredentialsException("Refresh token was revoked. Please login again.");
        }
        
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new InvalidCredentialsException("Refresh token expired. Please login again.");
        }
        
        return token;
    }
    
    /**
     * Kullanıcının tüm refresh tokenlarını iptal et (logout için)
     */
    @Transactional
    public void revokeAllUserTokens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found: " + username));
        refreshTokenRepository.deleteAllUserTokens(user);
    }
    
    /**
     * Süresi dolmuş tokenları temizle (scheduled task için)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}
