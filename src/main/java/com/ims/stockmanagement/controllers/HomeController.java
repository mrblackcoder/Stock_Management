package com.ims.stockmanagement.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    /**
     * Root endpoint - API status check
     */
    @GetMapping({"/", "/api"})
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "running");
        response.put("message", "Inventory Management System API is running");
        response.put("version", "1.0.0");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("api", "/api");
        endpoints.put("auth", "/api/auth/login, /api/auth/register");
        endpoints.put("products", "/api/products");
        endpoints.put("categories", "/api/categories");
        endpoints.put("suppliers", "/api/suppliers");
        endpoints.put("transactions", "/api/transactions");
        response.put("endpoints", endpoints);

        return response;
    }
}

