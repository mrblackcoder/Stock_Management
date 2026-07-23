package com.ims.stockmanagement.repositories;

import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.models.StockTransaction;
import com.ims.stockmanagement.models.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class StockTransactionRepositoryTest {

    private static final String FIXTURE_SKU = "TXN-SKU-001";
    private static final String FIXTURE_USERNAME = "txn_user";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    /**
     * Persists the minimum valid graph Category -> Product -> User -> StockTransaction
     * and returns the (still-managed) transaction. Supplier is intentionally left unset
     * because the Product mapping allows a null supplier.
     */
    private StockTransaction persistTransactionGraph() {
        Category category = new Category();
        category.setName("Electronics-" + FIXTURE_SKU);
        entityManager.persist(category);

        Product product = new Product();
        product.setName("Laptop Dell XPS 15");
        product.setSku(FIXTURE_SKU);
        product.setPrice(new BigDecimal("1500.00"));
        product.setStockQuantity(50);
        product.setReorderLevel(10);
        product.setCategory(category);
        entityManager.persist(product);

        User user = new User();
        user.setUsername(FIXTURE_USERNAME);
        user.setEmail(FIXTURE_USERNAME + "@example.com");
        user.setPassword("not-used-in-repository-test");
        user.setFullName("Transaction Test User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        entityManager.persist(user);

        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setQuantity(20);
        transaction.setUnitPrice(new BigDecimal("1500.00"));
        // totalPrice, status and transactionDate are populated by @PrePersist / entity defaults.
        entityManager.persist(transaction);

        return transaction;
    }

    @Test
    void findByIdWithProductAndUserLoadsProductAndUserEagerly() {
        StockTransaction persisted = persistTransactionGraph();

        entityManager.flush();               // push the inserts to the database
        Long transactionId = persisted.getId();
        entityManager.clear();               // detach everything: the query must reload via the fetch join

        Optional<StockTransaction> result = stockTransactionRepository.findByIdWithProductAndUser(transactionId);

        assertTrue(result.isPresent());
        StockTransaction transaction = result.get();
        assertEquals(transactionId, transaction.getId());
        assertTrue(Hibernate.isInitialized(transaction.getProduct()),
                "product must be eagerly initialized by the fetch join, not a lazy proxy");
        assertTrue(Hibernate.isInitialized(transaction.getUser()),
                "user must be eagerly initialized by the fetch join, not a lazy proxy");
        assertEquals(FIXTURE_SKU, transaction.getProduct().getSku());
        assertEquals(FIXTURE_USERNAME, transaction.getUser().getUsername());
    }

    @Test
    void findByIdWithProductAndUserUnknownIdReturnsEmpty() {
        Optional<StockTransaction> result = stockTransactionRepository.findByIdWithProductAndUser(999999L);

        assertFalse(result.isPresent());
    }
}
