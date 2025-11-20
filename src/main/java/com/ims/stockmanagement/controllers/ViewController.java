package com.ims.stockmanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// DISABLED: This controller is not used - React handles all routing
// @Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard";
    }

    @GetMapping("/products")
    public String productsPage(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "products";
    }

    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("pageTitle", "Categories");
        return "categories";
    }

    @GetMapping("/suppliers")
    public String suppliersPage(Model model) {
        model.addAttribute("pageTitle", "Suppliers");
        return "suppliers";
    }

    @GetMapping("/transactions")
    public String transactionsPage(Model model) {
        model.addAttribute("pageTitle", "Transactions");
        return "transactions";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("pageTitle", "Profile");
        return "profile";
    }
}

