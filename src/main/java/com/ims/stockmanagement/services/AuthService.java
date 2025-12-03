package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.LoginRequest;
import com.ims.stockmanagement.dtos.RegisterRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.UserDTO;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.exceptions.AccountLockedException;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.InvalidCredentialsException;
import com.ims.stockmanagement.models.RefreshToken;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import com.ims.stockmanagement.security.JwtService;
import com.ims.stockmanagement.security.LoginAttemptService;
import com.ims.stockmanagement.security.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final LoginAttemptService loginAttemptService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordValidator passwordValidator;

    /**
     * Yeni kullanıcı kaydı
     */
    public Response register(RegisterRequest request) {
        // Password strength validation
        List<String> passwordErrors = passwordValidator.validate(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            throw new IllegalArgumentException("Password validation failed: " + String.join("; ", passwordErrors));
        }
        
        // Kullanıcı adı kontrolü
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Email kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Yeni kullanıcı oluştur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER); // Varsayılan rol
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // JWT token oluştur
        String token = jwtService.generateToken(savedUser);

        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);

        return Response.builder()
                .statusCode(201)
                .message("User registered successfully")
                .token(token)
                .user(userDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Kullanıcı girişi
     * Brute Force Protection: Max 5 attempts, 15 minutes lockout
     */
    public Response login(LoginRequest request) {
        // 1. Check if user is blocked due to too many failed attempts
        if (loginAttemptService.isBlocked(request.getUsername())) {
            long unlockMinutes = loginAttemptService.getUnlockTimeMinutes(request.getUsername());
            throw new AccountLockedException(
                "Account temporarily locked due to too many failed login attempts. " +
                "Please try again in " + unlockMinutes + " minute(s)."
            );
        }

        try {
            // 2. Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 3. Authentication successful - reset failed attempts
            loginAttemptService.loginSucceeded(request.getUsername());

            // 4. Fetch user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

            // 5. Generate JWT token
            String token = jwtService.generateToken(user);
            
            // 6. Generate Refresh Token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            return Response.builder()
                    .statusCode(200)
                    .message("Login successful")
                    .token(token)
                    .refreshToken(refreshToken.getToken())
                    .user(userDTO)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (BadCredentialsException e) {
            // 6. Authentication failed - record failed attempt
            loginAttemptService.loginFailed(request.getUsername());

            int remainingAttempts = loginAttemptService.getRemainingAttempts(request.getUsername());

            if (remainingAttempts > 0) {
                throw new InvalidCredentialsException(
                    "Invalid username or password. " +
                    remainingAttempts + " attempt(s) remaining before account lockout."
                );
            } else {
                throw new AccountLockedException(
                    "Account locked due to too many failed login attempts. " +
                    "Please try again in 15 minutes."
                );
            }
        } catch (AccountLockedException e) {
            // Re-throw account locked exception
            throw e;
        } catch (Exception e) {
            // Handle other exceptions
            throw new InvalidCredentialsException("An error occurred during login. Please try again.");
        }
    }

    /**
     * Token'dan kullanıcı bilgisi al
     */
    public Response getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("User info retrieved successfully")
                .user(userDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Refresh token ile yeni access token al
     */
    public Response refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user);
                    UserDTO userDTO = modelMapper.map(user, UserDTO.class);
                    
                    return Response.builder()
                            .statusCode(200)
                            .message("Token refreshed successfully")
                            .token(newAccessToken)
                            .refreshToken(refreshTokenStr)
                            .user(userDTO)
                            .timestamp(LocalDateTime.now())
                            .build();
                })
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
    }
    
    /**
     * Logout - Tüm refresh tokenları iptal et
     */
    public Response logout(String username) {
        refreshTokenService.revokeAllUserTokens(username);
        
        return Response.builder()
                .statusCode(200)
                .message("Logged out successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
}

