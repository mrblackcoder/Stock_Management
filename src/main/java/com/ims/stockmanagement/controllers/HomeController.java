package com.ims.stockmanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    /**
     * Root endpoint - API status check
     */
    @GetMapping("/")
    @ResponseBody
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "running");
        response.put("message", "Inventory Management System API is running");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "api", "/api",
            "auth", "/api/auth/login, /api/auth/register",
            "products", "/api/products",
            "categories", "/api/categories",
            "suppliers", "/api/suppliers",
            "transactions", "/api/transactions"
        ));
        return response;
    }
}

