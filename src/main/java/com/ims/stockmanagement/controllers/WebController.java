package com.ims.stockmanagement.controllers;

// WebController previously defined duplicate mappings for /login, /register, /dashboard.
// These mappings are now provided by ViewController. To avoid ambiguous handler mappings
// we remove the @Controller annotation so this class will not be registered as a Spring MVC controller.
public class WebController {

    // kept methods as plain methods (not mapped) for reference / future use
    public String index() {
        return "redirect:/login";
    }

    public String login() {
        return "login";
    }

    public String register() {
        return "register";
    }

    public String dashboard() {
        return "dashboard";
    }
}
