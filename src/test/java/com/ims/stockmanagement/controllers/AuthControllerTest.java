package com.ims.stockmanagement.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.stockmanagement.dtos.LoginRequest;
import com.ims.stockmanagement.dtos.RegisterRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.exceptions.GlobalExceptionHandler;
import com.ims.stockmanagement.exceptions.InvalidCredentialsException;
import com.ims.stockmanagement.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private LoginRequest validLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("StrongPass@123");
        return request;
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password1"); // satisfies @Size(min=8) and the digit/lower/upper @Pattern
        request.setFullName("New User");
        return request;
    }

    @Test
    void login_returns200WithToken() throws Exception {
        Response serviceResponse = Response.builder()
                .statusCode(200)
                .message("Login successful")
                .token("jwt-abc")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.token").value("jwt-abc"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_wrongCredentialsReturns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message", containsString("Invalid")));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void register_invalidBodyReturns400() throws Exception {
        // Violates controller-level Bean Validation: blank username,
        // malformed email, blank password.
        RegisterRequest invalid = new RegisterRequest();
        invalid.setUsername("");
        invalid.setEmail("not-an-email");
        invalid.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void register_weakPasswordReturns400() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Password validation failed"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message", containsString("Password validation failed")));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }
}
