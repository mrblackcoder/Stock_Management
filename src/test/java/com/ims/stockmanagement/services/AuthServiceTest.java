package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.LoginRequest;
import com.ims.stockmanagement.dtos.RegisterRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import com.ims.stockmanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$EncodedPasswordHash");
        testUser.setFullName("Test User");
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$EncodedHash");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Response response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getUser());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        String generatedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null); // Successful authentication
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(generatedToken);

        // Act
        Response response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Login successful", response.getMessage());
        assertEquals(generatedToken, response.getToken());
        assertEquals("24Hr", response.getExpirationTime());
        assertEquals("USER", response.getRole());
        assertNotNull(response.getUser());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null); // Successful authentication
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testLogin_DisabledAccount() {
        // Arrange
        testUser.setEnabled(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.of(testUser));

        // Act
        Response response = authService.login(loginRequest);

        // Assert - Should still generate token, Spring Security handles account status
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testRegister_PasswordEncoded() {
        // Arrange
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$EncodedPasswordHash";

        registerRequest.setPassword(rawPassword);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(encodedPassword, savedUser.getPassword());
            return savedUser;
        });

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    void testRegister_DefaultRole() {
        // Arrange
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(UserRole.USER, savedUser.getRole());
            assertTrue(savedUser.isEnabled());
            return savedUser;
        });

        // Act
        authService.register(registerRequest);

        // Assert - Verified in mock setup above
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLogin_AdminRole() {
        // Arrange
        testUser.setRole(UserRole.ADMIN);
        String generatedToken = "admin-token-xyz";

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(generatedToken);

        // Act
        Response response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("ADMIN", response.getRole());
        assertEquals(generatedToken, response.getToken());
    }
}
