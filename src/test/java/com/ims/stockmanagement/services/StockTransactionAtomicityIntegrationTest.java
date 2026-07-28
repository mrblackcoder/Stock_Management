package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.exceptions.InsufficientStockException;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.CategoryRepository;
import com.ims.stockmanagement.repositories.ProductRepository;
import com.ims.stockmanagement.repositories.StockTransactionRepository;
import com.ims.stockmanagement.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real-database baseline: proves that, with today's implementation, a purchase or sale
 * atomically persists both the Product stock change and the matching StockTransaction, and
 * that a rejected sale leaves neither behind. Uses the real StockTransactionService and real
 * repositories against H2 - no mocks - so the assertions reflect actual persisted state, not
 * mocked interactions.
 *
 * This does not yet cover concurrent access; it establishes the single-request atomicity
 * baseline that later PR3 commits (locking) must continue to satisfy.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockTransactionAtomicityIntegrationTest {

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

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Category persistCategory(String suffix) {
        Category category = new Category();
        category.setName("atomicity-category-" + suffix);
        return categoryRepository.save(category);
    }

    private Product persistProduct(String suffix, Category category, int initialStock) {
        Product product = new Product();
        product.setName("Atomicity Test Product " + suffix);
        product.setSku("ATOMIC-SKU-" + suffix);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(initialStock);
        product.setReorderLevel(5);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private User persistUser(String suffix) {
        User user = new User();
        user.setUsername("atomicity_user_" + suffix);
        user.setEmail("atomicity_user_" + suffix + "@example.com");
        user.setPassword("not-used-in-this-test");
        user.setFullName("Atomicity Test User " + suffix);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Populates SecurityContextHolder exactly the way JwtAuthenticationFilter does in
     * production: a UsernamePasswordAuthenticationToken carrying the real User entity (which
     * implements UserDetails) as principal, so authentication.getName() resolves to the
     * username the same way it does at runtime. Deliberately not @WithMockUser, whose default
     * principal shape does not match what the service's SecurityContextHolder read expects.
     */
    private void authenticateAs(User user) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Test
    void purchasePersistsStockIncreaseAndTransactionTogether() {
        String suffix = uniqueSuffix();
        Category category = persistCategory(suffix);
        Product product = persistProduct(suffix, category, 50);
        User user = persistUser(suffix);
        authenticateAs(user);

        TransactionRequest request = new TransactionRequest();
        request.setProductId(product.getId());
        request.setQuantity(20);

        stockTransactionService.purchaseProduct(request);

        entityManager.flush();
        entityManager.clear();

        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(70, refreshedProduct.getStockQuantity());

        List<StockTransaction> transactions =
                stockTransactionRepository.findByProductWithRelations(refreshedProduct);
        assertEquals(1, transactions.size());
        StockTransaction transaction = transactions.get(0);
        assertEquals(TransactionType.PURCHASE, transaction.getTransactionType());
        assertEquals(20, transaction.getQuantity());
        assertEquals(user.getId(), transaction.getUser().getId());
    }

    @Test
    void salePersistsStockDecreaseAndTransactionTogether() {
        String suffix = uniqueSuffix();
        Category category = persistCategory(suffix);
        Product product = persistProduct(suffix, category, 50);
        User user = persistUser(suffix);
        authenticateAs(user);

        TransactionRequest request = new TransactionRequest();
        request.setProductId(product.getId());
        request.setQuantity(15);

        stockTransactionService.saleProduct(request);

        entityManager.flush();
        entityManager.clear();

        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(35, refreshedProduct.getStockQuantity());

        List<StockTransaction> transactions =
                stockTransactionRepository.findByProductWithRelations(refreshedProduct);
        assertEquals(1, transactions.size());
        StockTransaction transaction = transactions.get(0);
        assertEquals(TransactionType.SALE, transaction.getTransactionType());
        assertEquals(15, transaction.getQuantity());
        assertEquals(user.getId(), transaction.getUser().getId());
    }

    @Test
    void insufficientStockLeavesProductAndTransactionsUnchanged() {
        String suffix = uniqueSuffix();
        Category category = persistCategory(suffix);
        Product product = persistProduct(suffix, category, 5);
        User user = persistUser(suffix);
        authenticateAs(user);

        TransactionRequest request = new TransactionRequest();
        request.setProductId(product.getId());
        request.setQuantity(100); // exceeds the 5 units in stock

        assertThrows(InsufficientStockException.class,
                () -> stockTransactionService.saleProduct(request));

        entityManager.flush();
        entityManager.clear();

        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(5, refreshedProduct.getStockQuantity());

        List<StockTransaction> transactions =
                stockTransactionRepository.findByProductWithRelations(refreshedProduct);
        assertTrue(transactions.isEmpty());
    }
}
