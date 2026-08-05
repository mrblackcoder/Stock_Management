package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import com.ims.stockmanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The backend serves an API, not pages.
 *
 * A server-rendered controller used to map /, /login, /register and /dashboard to
 * Thymeleaf view names for templates that were never in the repository, so opening
 * the backend in a browser produced a template-resolution 500 - the first thing a
 * reviewer would hit. These cases pin the routes down as ordinary absent paths and
 * prove the API surface around them is untouched.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RemovedWebRouteIntegrationTest {

    private static final String AUTHENTICATION_REQUIRED = "Authentication is required to access this resource.";
    private static final String ACCESS_FORBIDDEN = "You do not have permission to access this resource.";
    private static final String NOT_FOUND = "The requested resource was not found.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private String userToken;

    @BeforeEach
    void setUp() {
        userToken = jwtService.generateToken(saveUser(UserRole.USER));
    }

    /**
     * Unauthenticated: the chain answers before routing, so these are 401 rather than
     * 404. That is deterministic and safe - the point is that no template resolution
     * is attempted and no 500 is produced.
     */
    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/register", "/dashboard"})
    void removedWebRouteIsNotServedToAnonymousCallers(String path) throws Exception {
        String body = mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_REQUIRED))
                .andReturn().getResponse().getContentAsString();

        assertNoTemplateFailure(body);
    }

    /** Authenticated: the path is simply absent, so it is a plain 404 - never a 500. */
    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/register", "/dashboard"})
    void removedWebRouteIsAbsentForAuthenticatedCallers(String path) throws Exception {
        String body = mockMvc.perform(get(path).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND))
                .andReturn().getResponse().getContentAsString();

        assertNoTemplateFailure(body);
        // The requested path is not echoed back.
        assertFalse(body.contains(path.substring(1)) && !path.equals("/"),
                "the 404 body must not repeat the requested path, body was: " + body);
    }

    @Test
    void apiStatusEndpointStillRespondsPublicly() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.message").value("Stock Management System API is running"))
                .andExpect(jsonPath("$.endpoints.products").value("/api/products"));
    }

    @Test
    void protectedApiStillRejectsAnonymousCallersWithTheSafe401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_REQUIRED));
    }

    @Test
    void protectedApiStillServesAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/api/products").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void adminOnlyApiStillRejectsANormalUser() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value(ACCESS_FORBIDDEN));
    }

    @Test
    void adminOnlyApiStillServesAnAdmin() throws Exception {
        String adminToken = jwtService.generateToken(saveUser(UserRole.ADMIN));

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    // Actuator is not asserted here: the test profile sets
    // management.endpoints.enabled-by-default=false, so its endpoints are absent
    // under test regardless of the security rules, which this change did not touch.

    private void assertNoTemplateFailure(String body) {
        String lower = body.toLowerCase();
        for (String marker : new String[]{"template", "thymeleaf", "templateinput", "exception"}) {
            assertFalse(lower.contains(marker),
                    "response must not reveal a template-resolution failure, found '" + marker + "' in: " + body);
        }
    }

    private User saveUser(UserRole role) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername("route_" + unique);
        user.setEmail("route_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth");
        user.setFullName("Route Fixture");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}
