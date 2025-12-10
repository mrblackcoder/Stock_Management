package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.LoginRequest;
import com.ims.stockmanagement.dtos.RefreshTokenRequest;
import com.ims.stockmanagement.dtos.RegisterRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@Valid @RequestBody RegisterRequest request) {
        Response response = authService.register(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@Valid @RequestBody LoginRequest request) {
        Response response = authService.login(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Response> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Response response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Response response = authService.logout(username);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
