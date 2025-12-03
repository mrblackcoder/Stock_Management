package com.ims.stockmanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller for Thymeleaf server-side rendered pages
 * Serves embedded interface as required by project specifications
 */
@Controller
public class WebController {

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
     * All other routes redirect to React SPA
     * This prevents 404 errors for routes that don't have Thymeleaf templates
     */
    @GetMapping({"/products", "/categories", "/suppliers", "/transactions", "/profile", "/users"})
    public String redirectToReact() {
        return "redirect:http://localhost:3000" + "?message=Please use React SPA for full features";
    }
}
