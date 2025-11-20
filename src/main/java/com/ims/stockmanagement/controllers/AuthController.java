package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.LoginRequest;
import com.ims.stockmanagement.dtos.RegisterRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody RegisterRequest request) {
        Response response = authService.register(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody LoginRequest request) {
        Response response = authService.login(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

