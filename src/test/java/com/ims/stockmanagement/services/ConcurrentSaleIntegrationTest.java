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
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;

/**
 * Proves that two concurrent sales of the last unit in stock cannot oversell: the
 * Product row lock serializes them, so one sale commits and the other is rejected with
 * InsufficientStockException against the freshly committed stock.
 *
 * Deliberately not @Transactional: each sale must run in its own committed transaction
 * on its own connection, otherwise there is nothing to serialize.
 *
 * Datasource-neutral by design - nothing here overrides spring.datasource.url, so this
 * runs against H2 locally and against the MySQL 8.0 service in GitHub Actions. Under H2
 * the JDBC URL needs a lock timeout longer than the negative-check window below (H2
 * defaults to 1s); supply it at runtime, e.g.
 * SPRING_DATASOURCE_URL='<test-url>;LOCK_TIMEOUT=10000'. MySQL's default
 * innodb_lock_wait_timeout of 50s already satisfies this.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentSaleIntegrationTest {

    private static final String FIRST_SALE_THREAD = "first-sale-thread";
    private static final String SECOND_SALE_THREAD = "second-sale-thread";

    /** One unit in stock, two buyers, one unit each: at most one can succeed. */
    private static final int INITIAL_STOCK = 1;
    private static final int SALE_QUANTITY = 1;

    /** How long the second sale is given to (wrongly) acquire the lock while the first holds it. */
    private static final long NEGATIVE_CHECK_MILLIS = 750L;
    private static final long LATCH_TIMEOUT_SECONDS = 20L;
    private static final long FUTURE_TIMEOUT_SECONDS = 30L;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private StockTransactionService stockTransactionService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private EntityManager entityManager;

    /** Real Spring Data proxy used by the spy to run the genuine locking query. */
    private ProductRepository unspiedRepository;

    private Long productId;
    private Long categoryId;
    private Long firstUserId;
    private Long secondUserId;

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
        if (firstUserId != null) {
            userRepository.findById(firstUserId).ifPresent(userRepository::delete);
        }
        if (secondUserId != null) {
            userRepository.findById(secondUserId).ifPresent(userRepository::delete);
        }
    }

    @Test
    void concurrentSalesOfTheLastUnitCannotOversell() throws Exception {
        User firstUser = persistUser("first");
        firstUserId = firstUser.getId();
        User secondUser = persistUser("second");
        secondUserId = secondUser.getId();
        persistProduct();

        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondLockAttempted = new CountDownLatch(1);
        CountDownLatch secondLockAcquired = new CountDownLatch(1);

        // Intercept only the two worker threads; main-thread fixture and assertion
        // lookups must run untouched.
        doAnswer(invocation -> {
            String threadName = Thread.currentThread().getName();

            if (FIRST_SALE_THREAD.equals(threadName)) {
                Optional<Product> result = realFindByIdForUpdate();
                // The row lock is held from here until this sale's transaction commits.
                firstLockAcquired.countDown();
                if (!releaseFirst.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("timed out waiting for the first-sale release signal");
                }
                return result;
            }

            if (SECOND_SALE_THREAD.equals(threadName)) {
                secondLockAttempted.countDown();
                Optional<Product> result = realFindByIdForUpdate();
                secondLockAcquired.countDown();
                return result;
            }

            return realFindByIdForUpdate();
        }).when(productRepository).findByIdForUpdate(productId);

        ExecutorService firstExecutor =
                Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, FIRST_SALE_THREAD));
        ExecutorService secondExecutor =
                Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, SECOND_SALE_THREAD));

        boolean secondAcquiredLockWhileFirstHeld;
        boolean secondAcquiredLockAfterRelease;
        Throwable firstFailure;
        Throwable secondFailure;
        try {
            Future<Void> firstFuture = firstExecutor.submit(saleTask(firstUser));

            if (!firstLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                // The worker died before signalling; surface its cause rather than a bare timeout.
                firstFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the first sale never acquired the product row lock");
            }

            Future<Void> secondFuture = secondExecutor.submit(saleTask(secondUser));

            if (!secondLockAttempted.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                secondFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the second sale never reached its lock attempt");
            }

            // Negative proof: the second sale is contending for the same row while the
            // first sale is still active, and must not be granted the lock.
            secondAcquiredLockWhileFirstHeld =
                    secondLockAcquired.await(NEGATIVE_CHECK_MILLIS, TimeUnit.MILLISECONDS);

            releaseFirst.countDown();

            firstFailure = failureOf(firstFuture);
            secondFailure = failureOf(secondFuture);

            secondAcquiredLockAfterRelease =
                    secondLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            // A failed assertion must never leave a worker pinned or a pool alive.
            releaseFirst.countDown();
            firstExecutor.shutdownNow();
            secondExecutor.shutdownNow();
            firstExecutor.awaitTermination(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            secondExecutor.awaitTermination(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        // The two sales really contended for the same product row.
        assertFalse(secondAcquiredLockWhileFirstHeld,
                "the second sale acquired the product row lock while the first sale was still active; "
                        + "the sale path is not serializing");
        assertTrue(secondAcquiredLockAfterRelease,
                "the second sale never acquired the product row lock after the first sale committed");

        // Exactly one sale succeeded, exactly one was rejected for insufficient stock.
        int successes = (firstFailure == null ? 1 : 0) + (secondFailure == null ? 1 : 0);
        assertEquals(1, successes, "exactly one of the two concurrent sales must succeed");

        Throwable rejection = firstFailure != null ? firstFailure : secondFailure;
        assertInstanceOf(InsufficientStockException.class, rejection,
                "the losing sale must be rejected with InsufficientStockException");

        Long successfulUserId = firstFailure == null ? firstUserId : secondUserId;

        // Committed stock: sold down to zero exactly once, never negative.
        Product committed = productRepository.findById(productId).orElseThrow();
        assertEquals(0, committed.getStockQuantity(), "the last unit must be sold exactly once");
        assertTrue(committed.getStockQuantity() >= 0, "stock must never go negative");

        // Ledger: one row, and it belongs to the sale that actually succeeded.
        List<StockTransaction> ledger =
                stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
        assertEquals(1, ledger.size(), "the rejected sale must not leave a partial or duplicate ledger row");

        StockTransaction sale = ledger.get(0);
        assertEquals(TransactionType.SALE, sale.getTransactionType());
        assertEquals(SALE_QUANTITY, sale.getQuantity());
        assertEquals(successfulUserId, sale.getUser().getId(),
                "the ledger actor must be the authenticated worker whose sale succeeded");
    }

    /**
     * SecurityContextHolder is thread-local: each worker authenticates itself and clears
     * the context on its own thread.
     */
    private Callable<Void> saleTask(User actor) {
        return () -> {
            try {
                authenticateAs(actor);
                TransactionRequest request = new TransactionRequest();
                request.setProductId(productId);
                request.setQuantity(SALE_QUANTITY);
                stockTransactionService.saleProduct(request);
                return null;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private Throwable failureOf(Future<Void> future) throws Exception {
        try {
            future.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return null;
        } catch (ExecutionException executionException) {
            return executionException.getCause();
        }
    }

    /**
     * Executes the genuine locking query.
     *
     * The spy cannot use Invocation.callRealMethod(): ProductRepository is an
     * interface-backed Spring Data proxy, so Mockito rejects the call with
     * "Cannot call abstract real method on java object". Instead the interception
     * delegates to a second, real Spring Data proxy built over the container's shared
     * EntityManager. It runs the same @Lock(PESSIMISTIC_WRITE) query and joins the
     * calling thread's transaction, so the row lock is acquired for real - no
     * production code or repository behaviour is altered to support the test.
     */
    private Optional<Product> realFindByIdForUpdate() {
        Optional<Product> result = realRepository().findByIdForUpdate(productId);
        if (result.isEmpty()) {
            throw new IllegalStateException("the locking lookup returned no product for id " + productId);
        }
        return result;
    }

    private ProductRepository realRepository() {
        if (unspiedRepository == null) {
            unspiedRepository = new JpaRepositoryFactory(entityManager).getRepository(ProductRepository.class);
        }
        return unspiedRepository;
    }

    private User persistUser(String role) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername("concurrent_sale_" + role + "_" + suffix);
        user.setEmail("concurrent_sale_" + role + "_" + suffix + "@example.com");
        user.setPassword("not-used-in-this-test");
        user.setFullName("Concurrent Sale " + role + " User " + suffix);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void persistProduct() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("concurrent-sale-category-" + suffix);
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setName("Concurrent Sale Test Product " + suffix);
        product.setSku("CSALE-SKU-" + suffix);
        product.setDescription("last unit in stock");
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(INITIAL_STOCK);
        product.setReorderLevel(0);
        product.setCategory(categoryRepository.findById(categoryId).orElseThrow());

        productId = productRepository.save(product).getId();
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
