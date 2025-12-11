package com.ims.stockmanagement.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller for Thymeleaf server-side rendered pages
 * Serves embedded interface as required by project specifications
 */
@Controller
public class WebController {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Serves the embedded login page (Thymeleaf)
     * Accessible at: http://localhost:8080/login
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Serves the embedded register page (Thymeleaf)
     * Accessible at: http://localhost:8080/register
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Serves the embedded dashboard page (Thymeleaf)
     * Accessible at: http://localhost:8080/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    /**
     * Root endpoint redirects to login
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    /**
     * Other routes redirect to React SPA using configurable URL
     * In production, set FRONTEND_URL environment variable
     */
    @GetMapping({"/products", "/categories", "/suppliers", "/transactions", "/profile", "/users"})
    public String redirectToReact() {
        return "redirect:" + frontendUrl;
    }
}
