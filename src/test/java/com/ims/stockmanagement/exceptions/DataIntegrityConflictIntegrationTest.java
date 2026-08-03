package com.ims.stockmanagement.exceptions;

import com.ims.stockmanagement.enums.TransactionStatus;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.StockTransactionRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import com.ims.stockmanagement.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A rejected delete must read as a business conflict, not as a database error report.
 *
 * Deleting a User that a committed Product and a committed StockTransaction still point
 * at hits a foreign key. The driver's message for that names the constraint, the tables
 * and the columns; this test pins that none of it reaches the caller.
 *
 * Deliberately not @Transactional: the fixtures and the delete attempt must really be
 * committed, otherwise the constraint would never be evaluated at all.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataIntegrityConflictIntegrationTest {

    private static final String CONFLICT_MESSAGE = "The operation conflicts with existing data.";

    /** Anything that would tell a caller how the data is stored. */
    private static final List<String> FORBIDDEN_DISCLOSURES = List.of(
            "constraint", "foreign key", "sql", "jdbc", "hibernate", "products", "stock_transactions", "fk");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockTransactionRepository transactionRepository;

    private Long adminId;
    private Long targetUserId;
    private Long productId;
    private Long categoryId;
    private Long transactionId;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        if (transactionId != null) {
            transactionRepository.findById(transactionId).ifPresent(transactionRepository::delete);
        }
        if (productId != null) {
            productRepository.findById(productId).ifPresent(productRepository::delete);
        }
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(categoryRepository::delete);
        }
        if (targetUserId != null) {
            userRepository.findById(targetUserId).ifPresent(userRepository::delete);
        }
        if (adminId != null) {
            userRepository.findById(adminId).ifPresent(userRepository::delete);
        }
    }

    @Test
    void deletingReferencedUserReturns409WithoutDatabaseDetails() throws Exception {
        User admin = persistUser("integrity_admin", UserRole.ADMIN);
        adminId = admin.getId();

        User target = persistUser("integrity_target", UserRole.USER);
        targetUserId = target.getId();

        persistProductAndTransaction(target);

        String body = mockMvc.perform(delete("/api/users/" + targetUserId)
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value(CONFLICT_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn().getResponse().getContentAsString();

        String lowerBody = body.toLowerCase();
        for (String disclosure : FORBIDDEN_DISCLOSURES) {
            assertFalse(lowerBody.contains(disclosure),
                    "conflict response must not disclose '" + disclosure + "', body was: " + body);
        }

        // The rejected delete changed nothing: the user and everything referencing it survive.
        assertTrue(userRepository.findById(targetUserId).isPresent(),
                "the referenced user must still exist after a rejected delete");
        assertTrue(productRepository.findById(productId).isPresent(),
                "the referencing product must survive");
        assertTrue(transactionRepository.findById(transactionId).isPresent(),
                "the referencing ledger row must survive");
    }

    private User persistUser(String prefix, UserRole role) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(prefix + "_" + unique);
        user.setEmail(prefix + "_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth");
        user.setFullName("Data Integrity " + prefix);
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void persistProductAndTransaction(User owner) {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("integrity-category-" + unique);
        category.setDescription("Data integrity fixture");
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setName("Data Integrity Product " + unique);
        product.setSku("INTEG-" + unique);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(5);
        product.setReorderLevel(1);
        product.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        product.setCreatedBy(owner);
        productId = productRepository.save(product).getId();

        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(productRepository.findById(productId).orElseThrow());
        transaction.setUser(owner);
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setQuantity(2);
        transaction.setUnitPrice(new BigDecimal("100.00"));
        transaction.setTotalPrice(new BigDecimal("200.00"));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionId = transactionRepository.save(transaction).getId();
    }
}
