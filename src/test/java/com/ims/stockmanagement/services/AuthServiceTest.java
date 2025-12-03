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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDTO testUserDTO;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

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

        // Setup test user DTO
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFullName("Test User");
        testUserDTO.setRole(UserRole.USER);

        // Setup register request with strong password
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("StrongPass@123");
        registerRequest.setFullName("New User");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("StrongPass@123");

        // Setup refresh token
        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("refresh-token-123");
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(604800));
        refreshToken.setRevoked(false);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(new ArrayList<>());
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$EncodedHash");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(testUserDTO);

        // Act
        Response response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getToken());
        assertNotNull(response.getUser());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
    }

    @Test
    void testRegister_WeakPassword() {
        // Arrange
        ArrayList<String> errors = new ArrayList<>();
        errors.add("Password must be at least 8 characters");
        when(passwordValidator.validate(anyString())).thenReturn(errors);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Password validation failed"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(new ArrayList<>());
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        AlreadyExistsException exception = assertThrows(
            AlreadyExistsException.class,
            () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(new ArrayList<>());
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        AlreadyExistsException exception = assertThrows(
            AlreadyExistsException.class,
            () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        String generatedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(loginAttemptService.isBlocked(loginRequest.getUsername())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null); // Successful authentication
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(generatedToken);
        when(refreshTokenService.createRefreshToken(testUser.getUsername())).thenReturn(refreshToken);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(testUserDTO);

        // Act
        Response response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Login successful", response.getMessage());
        assertEquals(generatedToken, response.getToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(testUser);
        verify(loginAttemptService, times(1)).loginSucceeded(loginRequest.getUsername());
    }

    @Test
    void testLogin_AccountLocked() {
        // Arrange
        when(loginAttemptService.isBlocked(loginRequest.getUsername())).thenReturn(true);
        when(loginAttemptService.getUnlockTimeMinutes(loginRequest.getUsername())).thenReturn(10L);

        // Act & Assert
        AccountLockedException exception = assertThrows(
            AccountLockedException.class,
            () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("locked"));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        when(loginAttemptService.isBlocked(loginRequest.getUsername())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));
        when(loginAttemptService.getRemainingAttempts(loginRequest.getUsername())).thenReturn(4);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid"));
        verify(loginAttemptService, times(1)).loginFailed(loginRequest.getUsername());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(loginAttemptService.isBlocked(loginRequest.getUsername())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByUsername(loginRequest.getUsername()))
            .thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid"));
    }

    @Test
    void testRefreshToken_Success() {
        // Arrange
        String newAccessToken = "new-access-token";
        when(refreshTokenService.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateToken(testUser)).thenReturn(newAccessToken);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(testUserDTO);

        // Act
        Response response = authService.refreshToken("refresh-token-123");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(newAccessToken, response.getToken());
        assertEquals("refresh-token-123", response.getRefreshToken());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Arrange
        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.refreshToken("invalid-token")
        );

        assertTrue(exception.getMessage().contains("Invalid refresh token"));
    }

    @Test
    void testLogout_Success() {
        // Arrange
        doNothing().when(refreshTokenService).revokeAllUserTokens("testuser");

        // Act
        Response response = authService.logout("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Logged out successfully", response.getMessage());
        verify(refreshTokenService, times(1)).revokeAllUserTokens("testuser");
    }

    @Test
    void testGetUserInfo_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(testUserDTO);

        // Act
        Response response = authService.getUserInfo("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void testGetUserInfo_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.getUserInfo("unknownuser")
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }
}
