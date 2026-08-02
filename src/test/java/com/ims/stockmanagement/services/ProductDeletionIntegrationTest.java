package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.exceptions.ProductHasTransactionHistoryException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real-database proof of the ledger-preservation policy: a Product with stock
 * transaction history cannot be hard deleted, and the attempt leaves both the Product
 * and every ledger row untouched. A Product with no history still deletes normally.
 *
 * Deliberately not @Transactional: the fixtures and the deletion attempt must be
 * committed, otherwise a rolled-back test could not distinguish "cascade did not fire"
 * from "nothing was ever written".
 *
 * Datasource-neutral - nothing here overrides spring.datasource.url, so this runs
 * against H2 locally and against the MySQL 8.0 service in GitHub Actions.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductDeletionIntegrationTest {

    private static final String HISTORY_MESSAGE =
            "Product cannot be deleted because it has stock transaction history.";

    private static final int INITIAL_STOCK = 25;
    private static final int PURCHASE_QUANTITY = 4;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockTransactionService stockTransactionService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    private Long productId;
    private Long categoryId;
    private Long userId;

    /**
     * Ledger rows are removed explicitly before the Product, because the absence of
     * cascade removal is exactly what these tests assert.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        if (productId != null) {
            stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId)
                    .forEach(stockTransactionRepository::delete);
            productRepository.findById(productId).ifPresent(productRepository::delete);
        }
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(categoryRepository::delete);
        }
        if (userId != null) {
            userRepository.findById(userId).ifPresent(userRepository::delete);
        }
    }

    @Test
    void deletingProductWithoutHistorySucceeds() {
        User owner = persistUser();
        Product product = persistProduct(owner);
        authenticateAs(owner);

        Response response = productService.deleteProduct(productId);

        assertEquals(200, response.getStatusCode());
        assertEquals("Product deleted successfully", response.getMessage());

        assertTrue(productRepository.findById(product.getId()).isEmpty(),
                "a product without ledger history must be deleted");
        assertTrue(stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(product.getId()).isEmpty(),
                "no ledger row should exist for a product that never had one");
    }

    @Test
    void deletingProductWithHistoryIsRejectedAndPreservesLedger() {
        User owner = persistUser();
        Product product = persistProduct(owner);
        authenticateAs(owner);

        // Committed ledger row created through the real transaction workflow.
        TransactionRequest purchase = new TransactionRequest();
        purchase.setProductId(productId);
        purchase.setQuantity(PURCHASE_QUANTITY);
        stockTransactionService.purchaseProduct(purchase);

        List<StockTransaction> before =
                stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
        assertFalse(before.isEmpty(), "fixture must produce at least one ledger row");
        Long transactionId = before.get(0).getId();
        int transactionCount = before.size();
        int stockBefore = productRepository.findById(productId).orElseThrow().getStockQuantity();

        ProductHasTransactionHistoryException exception = assertThrows(
                ProductHasTransactionHistoryException.class,
                () -> productService.deleteProduct(productId));
        assertEquals(HISTORY_MESSAGE, exception.getMessage());

        // The product survives the rejected deletion untouched.
        Product survivor = productRepository.findById(productId).orElseThrow();
        assertEquals(stockBefore, survivor.getStockQuantity(), "stock must be unchanged");

        // Every ledger row survives: nothing was cascade-deleted.
        List<StockTransaction> after =
                stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
        assertEquals(transactionCount, after.size(), "no ledger row may be removed");
        assertTrue(after.stream().anyMatch(entry -> entry.getId().equals(transactionId)),
                "the original transaction must still exist");
        assertTrue(stockTransactionRepository.existsById(transactionId),
                "the original transaction must still be readable by id");
    }

    private User persistUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername("deletion_policy_" + suffix);
        user.setEmail("deletion_policy_" + suffix + "@example.com");
        user.setPassword("not-used-in-this-test");
        user.setFullName("Deletion Policy User " + suffix);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        User saved = userRepository.save(user);
        userId = saved.getId();
        return saved;
    }

    private Product persistProduct(User owner) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("deletion-policy-category-" + suffix);
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setName("Deletion Policy Product " + suffix);
        product.setSku("DELPOL-SKU-" + suffix);
        product.setDescription("ledger preservation fixture");
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(INITIAL_STOCK);
        product.setReorderLevel(5);
        product.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        product.setCreatedBy(owner);

        Product saved = productRepository.save(product);
        productId = saved.getId();
        return saved;
    }

    /**
     * Matches how JwtAuthenticationFilter populates the context at runtime: the real User
     * entity (a UserDetails) as principal, so authentication.getName() resolves to the
     * username the services look up.
     */
    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
