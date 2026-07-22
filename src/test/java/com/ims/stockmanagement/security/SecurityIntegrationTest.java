package com.ims.stockmanagement.security;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real security integration test: full SecurityFilterChain, real JwtAuthenticationFilter,
 * real H2 + repositories, and the real JwtService. No mocked security, no @WithMockUser.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Persists a unique enabled USER directly to H2 (not the seeded admin, not a hardcoded id).
     * The username stored here is exactly the JWT subject the filter will reload.
     */
    private User saveUserFixture() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername("secuser_" + unique);
        user.setEmail("secuser_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth"); // non-null; JWT auth never checks the password
        user.setFullName("Security Test User");     // fullName is @Column(nullable = false)
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Test
    void protectedEndpointWithoutTokenReturns403() throws Exception {
        // No Authorization header: the chain rejects the anonymous request via the default entry point.
        // Records current baseline behavior (403); not a claim that 403 is the desired long-term API.
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointWithInvalidTokenReturns401() throws Exception {
        // A malformed Bearer token is rejected by JwtAuthenticationFilter, which writes a JSON 401.
        mockMvc.perform(get("/api/products/1")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void userEndpointWithValidUserTokenPassesAuthenticationAndAuthorization() throws Exception {
        User user = saveUserFixture();
        String token = jwtService.generateToken(user);

        // Valid USER token against a USER-authorized endpoint, for a product id that does not exist.
        // Must NOT be 401/403 (auth + USER authorization succeed); the downstream NotFoundException
        // is translated to 404. This proves the filter validated the JWT, reloaded the user from H2,
        // populated the SecurityContext, and the USER authority reached a USER-protected method.
        mockMvc.perform(get("/api/products/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void adminEndpointWithUserTokenReturns403() throws Exception {
        User user = saveUserFixture();
        String token = jwtService.generateToken(user);

        // The same valid USER token authenticates successfully but is denied an ADMIN-only method.
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
