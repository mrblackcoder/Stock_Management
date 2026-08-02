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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
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
    void adminEndpointWithUserTokenReturns403() throws Exception {
        User user = saveUserFixture("secuser");
        String token = jwtService.generateToken(user);

        // The same valid USER token authenticates successfully but is denied an ADMIN-only method.
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
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
