package com.ims.stockmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration for API Documentation
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.yourdomain.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Stock Management System API")
                .version("1.0.0")
                .description("""
                        ## Inventory Management System REST API

                        A comprehensive enterprise-grade API for managing inventory, products, categories, suppliers, and stock transactions.

                        ### Features:
                        - JWT Authentication & Authorization
                        - Role-based Access Control (ADMIN/USER)
                        - Complete Product Management
                        - Stock Transaction Tracking
                        - Low Stock Alerts
                        - CRUD Operations for all entities

                        ### Authentication:
                        1. Register a new account via `/api/auth/register`
                        2. Login via `/api/auth/login` to get JWT token
                        3. Click "Authorize" button and enter: `Bearer YOUR_TOKEN`
                        4. All protected endpoints will now work

                        ### Student Information:
                        - **Name:** Mehmet Taha Boynikoğlu
                        - **Student ID:** 212 125 10 34
                        - **Course:** Web Design and Programming
                        - **University:** Fatih Sultan Mehmet Vakıf Üniversitesi
                        """)
                .contact(new Contact()
                        .name("Mehmet Taha Boynikoğlu")
                        .email("your-email@example.com")
                        .url("https://github.com/mrblackcoder/Stock_Management"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Enter JWT token obtained from /api/auth/login endpoint");
    }
}
