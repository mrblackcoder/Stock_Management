package com.ims.stockmanagement.security;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.StockTransactionRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private static final String AUTHENTICATION_REQUIRED_MESSAGE =
            "Authentication is required to access this resource.";
    private static final String ACCESS_FORBIDDEN_MESSAGE =
            "You do not have permission to access this resource.";
    private static final String AUTHENTICATION_INVALID_MESSAGE =
            "Authentication is invalid.";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    /**
     * Persists a unique enabled USER directly to H2 (not the seeded admin, not a hardcoded id).
     * The username stored here is exactly the JWT subject the filter will reload.
     */
    private User saveUserFixture(String prefix) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(prefix + "_" + unique);
        user.setEmail(prefix + "_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth"); // non-null; JWT auth never checks the password
        user.setFullName("Security Test User");     // fullName is @Column(nullable = false)
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Product saveProductFixture() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        Category category = new Category();
        category.setName("security-category-" + unique);
        category.setDescription("Security integration test category");
        categoryRepository.save(category);

        Product product = new Product();
        product.setName("Security Test Product " + unique);
        product.setSku("SEC-" + unique);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(10);
        product.setReorderLevel(1);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private String transactionRequestBody(Long productId, String extraField) {
        return "{\"productId\":%d,\"transactionType\":\"PURCHASE\",\"quantity\":1%s}"
                .formatted(productId, extraField);
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Correctly signed for this application, but already past its expiry. */
    private String expiredTokenFor(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(now - 7_200_000))
                .expiration(new Date(now - 3_600_000))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Header and payload this application would accept, carrying a signature produced
     * with a different key - the shape of a forged or in-flight-modified token.
     */
    private String tamperedSignatureTokenFor(User user) {
        String valid = jwtService.generateToken(user);
        SecretKey foreignKey = Keys.hmacShaKeyFor(
                "a-completely-different-signing-key-of-sufficient-length-256".getBytes(StandardCharsets.UTF_8));
        String forged = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(foreignKey)
                .compact();

        return valid.substring(0, valid.lastIndexOf('.') + 1)
                + forged.substring(forged.lastIndexOf('.') + 1);
    }

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        // No Authorization header: the configured entry point answers "authenticate first"
        // with a stable JSON 401, instead of the framework default 403 that made a missing
        // credential indistinguishable from a real authorization denial.
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_REQUIRED_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void protectedEndpointWithInvalidTokenReturns401() throws Exception {
        // A malformed Bearer token is rejected by JwtAuthenticationFilter, which writes a JSON 401.
        String body = mockMvc.perform(get("/api/products/1")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        assertSafeSecurityErrorBody(body);
    }

    @Test
    void adminEndpointWithUserTokenReturns403() throws Exception {
        User user = saveUserFixture("secuser");
        String token = jwtService.generateToken(user);

        // The same valid USER token authenticates successfully but is denied an ADMIN-only method.
        String body = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value(ACCESS_FORBIDDEN_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn().getResponse().getContentAsString();

        assertSafeSecurityErrorBody(body);
    }

    @Test
    void disabledUserTokenReturns401() throws Exception {
        User user = saveUserFixture("secuser");
        // Token minted while the account was still usable, exactly like one already in a
        // client's hands when an administrator disables the account.
        String token = jwtService.generateToken(user);

        user.setEnabled(false);
        userRepository.save(user);

        String body = mockMvc.perform(get("/api/products/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_INVALID_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn().getResponse().getContentAsString();

        assertSafeSecurityErrorBody(body);
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "a disabled account must never reach the SecurityContext");
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        User user = saveUserFixture("secuser");
        String expiredToken = expiredTokenFor(user);

        String body = mockMvc.perform(get("/api/products/999999")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        assertSafeSecurityErrorBody(body);
    }

    @Test
    void tamperedSignatureReturns401() throws Exception {
        User user = saveUserFixture("secuser");
        String tamperedToken = tamperedSignatureTokenFor(user);

        String body = mockMvc.perform(get("/api/products/999999")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        assertSafeSecurityErrorBody(body);
    }

    /**
     * A security error body may state what the caller must do next and nothing else:
     * no exception class, no stack frame, no token content, no endpoint internals.
     */
    private void assertSafeSecurityErrorBody(String body) {
        String lower = body.toLowerCase();
        for (String leak : List.of("exception", "java.", "org.springframework", "io.jsonwebtoken",
                "stacktrace", "at com.ims", "bearer", "jwt", "sql")) {
            assertFalse(lower.contains(leak),
                    "security error body must not expose internals, found '" + leak + "' in: " + body);
        }
    }

    @Test
    void userEndpointWithValidUserTokenPassesAuthenticationAndAuthorization() throws Exception {
        User user = saveUserFixture("secuser");
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
    void transactionUsesAuthenticatedActorInsteadOfAnotherExistingUser() throws Exception {
        User actor = saveUserFixture("transaction_actor");
        User victim = saveUserFixture("transaction_victim");
        Product product = saveProductFixture();
        String token = jwtService.generateToken(actor);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestBody(product.getId(), "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction.userId").value(actor.getId()));

        StockTransaction transaction = stockTransactionRepository
                .findByProductIdOrderByTransactionDateDesc(product.getId())
                .getFirst();
        assertEquals(actor.getId(), transaction.getUser().getId());
        assertNotEquals(victim.getId(), transaction.getUser().getId());
    }

    @Test
    void transactionRequestWithClientSuppliedUserIdIsRejected() throws Exception {
        User actor = saveUserFixture("transaction_actor");
        User victim = saveUserFixture("transaction_victim");
        Product product = saveProductFixture();
        int initialStockQuantity = product.getStockQuantity();
        int initialTransactionCount = stockTransactionRepository
                .findByProductIdOrderByTransactionDateDesc(product.getId())
                .size();
        String token = jwtService.generateToken(actor);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestBody(product.getId(), ",\"userId\":" + victim.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400));

        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        List<StockTransaction> transactions = stockTransactionRepository
                .findByProductIdOrderByTransactionDateDesc(product.getId());
        assertEquals(initialStockQuantity, reloadedProduct.getStockQuantity());
        assertEquals(initialTransactionCount, transactions.size());
        assertTrue(transactions.stream().noneMatch(transaction ->
                actor.getId().equals(transaction.getUser().getId()) ||
                        victim.getId().equals(transaction.getUser().getId())));
    }
}
