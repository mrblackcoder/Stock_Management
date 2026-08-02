package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionRequest;
import com.ims.stockmanagement.enums.TransactionType;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.exceptions.ProductHasTransactionHistoryException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;

/**
 * Proves that Product deletion and stock transaction creation serialize on the same
 * Product row lock, in both possible orderings, with a deterministic outcome each time:
 *
 *  - transaction first: the deletion waits, then sees the committed history and is
 *    rejected, leaving the Product and its ledger intact;
 *  - deletion first: the transaction waits, then finds the Product already gone and
 *    fails with the existing not-found behaviour, leaving no ledger row behind.
 *
 * Neither ordering can produce a partial deletion, a silently destroyed ledger, or an
 * unhandled foreign-key race.
 *
 * Deliberately not @Transactional: both sides must run in their own committed
 * transactions on their own connections, otherwise there is nothing to serialize.
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
class ProductDeletionConcurrencyIntegrationTest {

    private static final String SALE_THREAD = "deletion-concurrency-sale-thread";
    private static final String DELETION_THREAD = "deletion-concurrency-deletion-thread";

    private static final String HISTORY_MESSAGE =
            "Product cannot be deleted because it has stock transaction history.";

    private static final int INITIAL_STOCK = 10;
    private static final int SALE_QUANTITY = 3;

    /** How long the waiting worker is given to (wrongly) get past the lock. */
    private static final long NEGATIVE_CHECK_MILLIS = 750L;
    private static final long LATCH_TIMEOUT_SECONDS = 20L;
    private static final long FUTURE_TIMEOUT_SECONDS = 30L;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

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
    private Long ownerUserId;
    private Long saleUserId;

    /**
     * Ledger rows are removed before the Product because the Product-to-transaction
     * cascade is deliberately gone.
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
        if (ownerUserId != null) {
            userRepository.findById(ownerUserId).ifPresent(userRepository::delete);
        }
        if (saleUserId != null) {
            userRepository.findById(saleUserId).ifPresent(userRepository::delete);
        }
    }

    /**
     * Ordering A: the sale takes the row lock first. The deletion must wait, and once it
     * finally runs it must observe the freshly committed ledger row and refuse.
     */
    @Test
    void transactionFirst_causesDeletionToWaitThenRejectsDeletion() throws Exception {
        User owner = persistUser("owner");
        ownerUserId = owner.getId();
        User saleActor = persistUser("sale");
        saleUserId = saleActor.getId();
        persistProduct(owner);

        CountDownLatch transactionLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseTransaction = new CountDownLatch(1);
        CountDownLatch deletionLockAttempted = new CountDownLatch(1);
        CountDownLatch deletionLockAcquired = new CountDownLatch(1);

        doAnswer(invocation -> {
            String threadName = Thread.currentThread().getName();

            if (SALE_THREAD.equals(threadName)) {
                Optional<Product> result = delegateFindByIdForUpdate();
                // Row lock held from here until the sale transaction commits.
                transactionLockAcquired.countDown();
                awaitRelease(releaseTransaction, "sale");
                return result;
            }

            if (DELETION_THREAD.equals(threadName)) {
                deletionLockAttempted.countDown();
                Optional<Product> result = delegateFindByIdForUpdate();
                deletionLockAcquired.countDown();
                return result;
            }

            return delegateFindByIdForUpdate();
        }).when(productRepository).findByIdForUpdate(productId);

        ExecutorService saleExecutor = namedExecutor(SALE_THREAD);
        ExecutorService deletionExecutor = namedExecutor(DELETION_THREAD);

        boolean deletionPassedLockWhileSaleHeld;
        boolean deletionAcquiredLockAfterRelease;
        Throwable saleFailure;
        Throwable deletionFailure;
        try {
            Future<Void> saleFuture = saleExecutor.submit(saleTask(saleActor));

            if (!transactionLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                saleFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the sale never acquired the product row lock");
            }

            Future<Void> deletionFuture = deletionExecutor.submit(deletionTask(owner));

            if (!deletionLockAttempted.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                deletionFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the deletion never reached its lock attempt");
            }

            // Negative proof: the deletion is contending for the same row and must wait.
            deletionPassedLockWhileSaleHeld =
                    deletionLockAcquired.await(NEGATIVE_CHECK_MILLIS, TimeUnit.MILLISECONDS);

            releaseTransaction.countDown();

            saleFailure = failureOf(saleFuture);
            deletionFailure = failureOf(deletionFuture);

            deletionAcquiredLockAfterRelease =
                    deletionLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            releaseTransaction.countDown();
            shutdown(saleExecutor, deletionExecutor);
        }

        // Ordering proof.
        assertFalse(deletionPassedLockWhileSaleHeld,
                "the deletion acquired the product row lock while the sale still held it");
        assertTrue(deletionAcquiredLockAfterRelease,
                "the deletion never acquired the product row lock after the sale committed");

        // The sale succeeded; the deletion was refused by the domain rule.
        assertNull(saleFailure, "the sale must succeed");
        assertInstanceOf(ProductHasTransactionHistoryException.class, deletionFailure,
                "the deletion must be rejected because history now exists");
        assertEquals(HISTORY_MESSAGE, deletionFailure.getMessage());

        // The product survived, carrying exactly the committed sale.
        Product survivor = productRepository.findById(productId).orElseThrow();
        assertEquals(INITIAL_STOCK - SALE_QUANTITY, survivor.getStockQuantity());
        assertTrue(survivor.getStockQuantity() >= 0, "stock must never go negative");

        List<StockTransaction> ledger =
                stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
        assertEquals(1, ledger.size(), "exactly one ledger row must exist");
        StockTransaction sale = ledger.get(0);
        assertEquals(TransactionType.SALE, sale.getTransactionType());
        assertEquals(SALE_QUANTITY, sale.getQuantity());
        assertEquals(saleUserId, sale.getUser().getId(),
                "the ledger actor must be the authenticated sale worker");
    }

    /**
     * Ordering B: the deletion takes the row lock first. The sale must wait, and once its
     * locking query finally runs the Product is already gone, so the existing not-found
     * behaviour applies and no ledger row is ever written.
     */
    @Test
    void deletionFirst_causesTransactionToWaitThenFailNotFound() throws Exception {
        User owner = persistUser("owner");
        ownerUserId = owner.getId();
        User saleActor = persistUser("sale");
        saleUserId = saleActor.getId();
        persistProduct(owner);

        CountDownLatch deletionLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseDeletion = new CountDownLatch(1);
        CountDownLatch transactionLockAttempted = new CountDownLatch(1);
        // Named "query completed", not "lock acquired": after the deletion commits this
        // query legitimately returns an empty Optional, so no Product entity is acquired.
        CountDownLatch transactionLockQueryCompleted = new CountDownLatch(1);

        doAnswer(invocation -> {
            String threadName = Thread.currentThread().getName();

            if (DELETION_THREAD.equals(threadName)) {
                Optional<Product> result = delegateFindByIdForUpdate();
                // Row lock held from here until the deletion transaction commits, so the
                // history check and the delete both happen under this lock.
                deletionLockAcquired.countDown();
                awaitRelease(releaseDeletion, "deletion");
                return result;
            }

            if (SALE_THREAD.equals(threadName)) {
                transactionLockAttempted.countDown();
                Optional<Product> result = delegateFindByIdForUpdate();
                transactionLockQueryCompleted.countDown();
                return result;
            }

            return delegateFindByIdForUpdate();
        }).when(productRepository).findByIdForUpdate(productId);

        ExecutorService deletionExecutor = namedExecutor(DELETION_THREAD);
        ExecutorService saleExecutor = namedExecutor(SALE_THREAD);

        boolean saleQueryCompletedWhileDeletionHeld;
        boolean saleQueryCompletedAfterRelease;
        Throwable deletionFailure;
        Throwable saleFailure;
        try {
            Future<Void> deletionFuture = deletionExecutor.submit(deletionTask(owner));

            if (!deletionLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                deletionFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the deletion never acquired the product row lock");
            }

            Future<Void> saleFuture = saleExecutor.submit(saleTask(saleActor));

            if (!transactionLockAttempted.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                saleFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the sale never reached its lock attempt");
            }

            // Negative proof: the sale's locking query is blocked behind the deletion.
            saleQueryCompletedWhileDeletionHeld =
                    transactionLockQueryCompleted.await(NEGATIVE_CHECK_MILLIS, TimeUnit.MILLISECONDS);

            releaseDeletion.countDown();

            deletionFailure = failureOf(deletionFuture);
            saleFailure = failureOf(saleFuture);

            saleQueryCompletedAfterRelease =
                    transactionLockQueryCompleted.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            releaseDeletion.countDown();
            shutdown(deletionExecutor, saleExecutor);
        }

        // Ordering proof.
        assertFalse(saleQueryCompletedWhileDeletionHeld,
                "the sale's locking query completed while the deletion still held the row lock");
        assertTrue(saleQueryCompletedAfterRelease,
                "the sale's locking query never completed after the deletion committed");

        // The deletion succeeded normally; the sale hit the existing not-found path.
        assertNull(deletionFailure, "the deletion must succeed when no history exists");
        assertInstanceOf(NotFoundException.class, saleFailure,
                "the sale must fail with the existing not-found behaviour, not a database error");

        // No raw persistence failure escaped to the caller.
        assertFalse(saleFailure.getClass().getName().startsWith("org.springframework.dao"),
                "no raw data-access exception may escape: " + saleFailure);
        assertFalse(saleFailure.getClass().getName().startsWith("org.hibernate"),
                "no raw Hibernate exception may escape: " + saleFailure);

        // The product is gone and nothing was written for it.
        assertTrue(productRepository.findById(productId).isEmpty(), "the product must be deleted");
        assertTrue(stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId).isEmpty(),
                "the rejected sale must not leave a partial ledger row");
        assertFalse(stockTransactionRepository.existsByProductId(productId),
                "no ledger row may reference the deleted product");
    }

    // ---------------------------------------------------------------- workers

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

    private Callable<Void> deletionTask(User actor) {
        return () -> {
            try {
                authenticateAs(actor);
                Response response = productService.deleteProduct(productId);
                assertEquals(200, response.getStatusCode());
                assertEquals("Product deleted successfully", response.getMessage());
                return null;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    // ---------------------------------------------------------------- helpers

    private ExecutorService namedExecutor(String threadName) {
        return Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, threadName));
    }

    private void shutdown(ExecutorService... executors) throws InterruptedException {
        for (ExecutorService executor : executors) {
            executor.shutdownNow();
        }
        for (ExecutorService executor : executors) {
            executor.awaitTermination(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void awaitRelease(CountDownLatch latch, String worker) throws InterruptedException {
        if (!latch.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("timed out waiting for the " + worker + " release signal");
        }
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
     *
     * An empty Optional is returned as-is rather than treated as an error: after a
     * committed deletion that is the correct, expected result of the locking query.
     */
    private Optional<Product> delegateFindByIdForUpdate() {
        return realRepository().findByIdForUpdate(productId);
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
        user.setUsername("deletion_race_" + role + "_" + suffix);
        user.setEmail("deletion_race_" + role + "_" + suffix + "@example.com");
        user.setPassword("not-used-in-this-test");
        user.setFullName("Deletion Race " + role + " User " + suffix);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void persistProduct(User owner) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("deletion-race-category-" + suffix);
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setName("Deletion Race Product " + suffix);
        product.setSku("DELRACE-SKU-" + suffix);
        product.setDescription("deletion versus transaction ordering fixture");
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(INITIAL_STOCK);
        product.setReorderLevel(1);
        product.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        product.setCreatedBy(owner);

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
