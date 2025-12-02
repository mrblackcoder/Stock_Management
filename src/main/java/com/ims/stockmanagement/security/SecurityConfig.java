package com.ims.stockmanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Enhanced Security Headers
                .headers(headers -> headers
                        // Prevent clickjacking attacks
                        .frameOptions(frame -> frame.deny())

                        // Prevent MIME type sniffing
                        .contentTypeOptions(contentType -> contentType.disable())

                        // Enable XSS protection
                        .xssProtection(xss -> xss
                                .headerValue("1; mode=block")
                        )

                        // Force HTTPS (HSTS) - Commented for development
                        // .httpStrictTransportSecurity(hsts -> hsts
                        //         .includeSubDomains(true)
                        //         .maxAgeInSeconds(31536000)
                        // )

                        // Content Security Policy
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
                                        "font-src 'self' https://cdnjs.cloudflare.com https://fonts.gstatic.com; " +
                                        "img-src 'self' data: https:; " +
                                        "connect-src 'self' http://localhost:* https://api.exchangerate-api.com;")
                        )

                        // Referrer Policy
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )

                        // Permissions Policy (formerly Feature Policy)
                        .permissionsPolicy(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=()")
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**", "/api/public/**", "/api").permitAll()

                        // Swagger/OpenAPI endpoints - public access for API documentation
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // Actuator endpoints - public health checks
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").authenticated()  // Other actuator endpoints require auth

                        // Frontend pages - no authentication required
                        .requestMatchers("/", "/login", "/register", "/dashboard", "/products",
                                "/categories", "/suppliers", "/transactions", "/profile").permitAll()

                        // Static resources - no authentication required
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()

                        // API endpoints - All authenticated users can access (ORDER MATTERS!)
                        .requestMatchers("/api/products/**").authenticated()
                        .requestMatchers("/api/categories/**").authenticated()
                        .requestMatchers("/api/suppliers/**").authenticated()
                        .requestMatchers("/api/transactions/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()

                        // Default: require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Role Hierarchy Configuration
     * Defines hierarchical role relationships:
     * - ADMIN inherits all permissions from USER
     * - This means ADMIN can access endpoints marked with @PreAuthorize("hasRole('USER')")
     *
     * Benefits:
     * - Cleaner code: No need to write @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
     * - Easier maintenance: Add new roles without changing existing security annotations
     * - Follows principle of least privilege
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }
}
