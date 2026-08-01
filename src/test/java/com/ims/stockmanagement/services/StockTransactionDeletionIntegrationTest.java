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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class StockTransactionDeletionIntegrationTest {

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
    void rejectedPurchaseReversalLeavesCommittedProductAndTransactionUnchanged() {
        User user = persistUser();
        Product product = persistProduct(50);
        authenticateAs(user);

        stockTransactionService.purchaseProduct(transactionRequest(product.getId(), 20));
        stockTransactionService.saleProduct(transactionRequest(product.getId(), 65));

        Long purchaseTransactionId = stockTransactionRepository
                .findByProductIdOrderByTransactionDateDesc(product.getId())
                .stream()
                .filter(transaction -> transaction.getTransactionType() == TransactionType.PURCHASE)
                .findFirst()
                .orElseThrow()
                .getId();

        assertThrows(InsufficientStockException.class,
                () -> stockTransactionService.deleteTransaction(purchaseTransactionId));

        Product reloadedProduct = productRepository.findById(product.getId()).orElseThrow();
        List<StockTransaction> transactions =
                stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(product.getId());
        assertEquals(5, reloadedProduct.getStockQuantity());
        assertTrue(transactions.stream().anyMatch(transaction ->
                transaction.getId().equals(purchaseTransactionId)));
    }

    private User persistUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername("deletion_user_" + suffix);
        user.setEmail("deletion_user_" + suffix + "@example.com");
        user.setPassword("not-used-in-this-test");
        user.setFullName("Deletion Test User " + suffix);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();
        return savedUser;
    }

    private Product persistProduct(int initialStock) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Category category = new Category();
        category.setName("deletion-category-" + suffix);
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setName("Deletion Test Product " + suffix);
        product.setSku("DELETE-SKU-" + suffix);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(initialStock);
        product.setReorderLevel(5);
        product.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        Product savedProduct = productRepository.save(product);
        productId = savedProduct.getId();
        return savedProduct;
    }

    private TransactionRequest transactionRequest(Long requestedProductId, int quantity) {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(requestedProductId);
        request.setTransactionType(TransactionType.PURCHASE);
        request.setQuantity(quantity);
        return request;
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
