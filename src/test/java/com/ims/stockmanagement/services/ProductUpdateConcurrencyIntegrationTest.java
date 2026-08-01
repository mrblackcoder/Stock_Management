package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.ProductUpdateRequest;
import com.ims.stockmanagement.dtos.TransactionRequest;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;

/**
 * Proves that an ordinary descriptive product edit and a concurrent sale of the same
 * product serialize on the Product row lock, and that the edit cannot resurrect the
 * pre-sale stock value.
 *
 * Deliberately not @Transactional: both sides must run in their own committed
 * transactions on their own connections, otherwise there is nothing to serialize.
 *
 * Datasource-neutral by design - no @TestPropertySource overrides spring.datasource.url,
 * so this runs against H2 locally and against the MySQL 8.0 service in GitHub Actions.
 * Under H2 the JDBC URL needs a lock timeout comfortably longer than the negative-check
 * window below (H2 defaults to 1s); supply it at runtime, e.g.
 * SPRING_DATASOURCE_URL='<test-url>;LOCK_TIMEOUT=10000'. MySQL's default
 * innodb_lock_wait_timeout of 50s already satisfies this.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductUpdateConcurrencyIntegrationTest {

    private static final String EDIT_THREAD = "product-edit-thread";
    private static final String SALE_THREAD = "product-sale-thread";

    private static final int INITIAL_STOCK = 40;
    private static final int SALE_QUANTITY = 6;

    /** How long the sale is given to (wrongly) acquire the lock while the edit holds it. */
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
    private Long editUserId;
    private Long saleUserId;

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
        if (editUserId != null) {
            userRepository.findById(editUserId).ifPresent(userRepository::delete);
        }
        if (saleUserId != null) {
            userRepository.findById(saleUserId).ifPresent(userRepository::delete);
        }
    }

    @Test
    void saleBlocksWhileDescriptiveUpdateHoldsRowLock() throws Exception {
        ConcurrencyOutcome outcome = runConcurrentEditAndSale();

        assertFalse(outcome.saleAcquiredLockWhileEditHeld(),
                "the sale acquired the product row lock while the descriptive update still held it; "
                        + "the update path is not serializing against stock transactions");
        assertTrue(outcome.saleAcquiredLockAfterRelease(),
                "the sale never acquired the product row lock after the edit transaction committed");
    }

    @Test
    void descriptiveEditAndSaleBothPersistExactlyOnce() throws Exception {
        ConcurrencyOutcome outcome = runConcurrentEditAndSale();

        Product committed = productRepository.findById(outcome.productId()).orElseThrow();

        // The descriptive edit survived.
        assertEquals(outcome.editedName(), committed.getName());
        assertEquals(outcome.editedDescription(), committed.getDescription());
        assertEquals(0, outcome.editedPrice().compareTo(committed.getPrice()));

        // The sale was applied exactly once and the stale pre-sale value was not restored.
        assertEquals(INITIAL_STOCK - SALE_QUANTITY, committed.getStockQuantity());
        assertNotEquals(INITIAL_STOCK, committed.getStockQuantity());

        List<StockTransaction> ledger =
                stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(outcome.productId());
        List<StockTransaction> sales = ledger.stream()
                .filter(entry -> entry.getTransactionType() == TransactionType.SALE)
                .toList();
        assertEquals(1, sales.size(), "expected exactly one SALE ledger row");
        assertEquals(SALE_QUANTITY, sales.get(0).getQuantity());
        assertEquals(outcome.saleUserId(), sales.get(0).getUser().getId(),
                "the ledger actor must be the authenticated sale worker");
    }

    /**
     * Runs one descriptive update and one sale of the same product on two named threads,
     * with the update pinned open while it holds the row lock.
     *
     * Ordering is established by latches only - never by sleeping - and the negative check
     * is made only after the sale thread has been observed reaching its lock attempt, so a
     * pass cannot come from the sale task simply not having started yet.
     */
    private ConcurrencyOutcome runConcurrentEditAndSale() throws Exception {
        User editUser = persistUser("edit");
        editUserId = editUser.getId();
        User saleUser = persistUser("sale");
        saleUserId = saleUser.getId();
        Product product = persistProduct();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String editedName = "Renamed Product " + suffix;
        String editedDescription = "Edited concurrently with a sale " + suffix;
        BigDecimal editedPrice = new BigDecimal("321.50");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName(editedName);
        updateRequest.setSku(product.getSku()); // unchanged
        updateRequest.setDescription(editedDescription);
        updateRequest.setPrice(editedPrice);
        updateRequest.setReorderLevel(3);
        updateRequest.setCategoryId(categoryId);

        CountDownLatch editLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseEdit = new CountDownLatch(1);
        CountDownLatch saleLockAttempted = new CountDownLatch(1);
        CountDownLatch saleLockAcquired = new CountDownLatch(1);

        // Intercept only the two worker threads; main-thread fixture and assertion
        // lookups must run untouched.
        doAnswer(invocation -> {
            String threadName = Thread.currentThread().getName();

            if (EDIT_THREAD.equals(threadName)) {
                Optional<Product> result = realFindByIdForUpdate();
                // The row lock is held from here until this transaction commits.
                editLockAcquired.countDown();
                if (!releaseEdit.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("timed out waiting for the edit release signal");
                }
                return result;
            }

            if (SALE_THREAD.equals(threadName)) {
                saleLockAttempted.countDown();
                Optional<Product> result = realFindByIdForUpdate();
                saleLockAcquired.countDown();
                return result;
            }

            return realFindByIdForUpdate();
        }).when(productRepository).findByIdForUpdate(productId);

        ExecutorService editExecutor =
                Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, EDIT_THREAD));
        ExecutorService saleExecutor =
                Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, SALE_THREAD));

        Callable<Void> editTask = () -> {
            // SecurityContextHolder is thread-local: each worker authenticates itself.
            try {
                authenticateAs(editUser);
                productService.updateProduct(productId, updateRequest);
                return null;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        Callable<Void> saleTask = () -> {
            try {
                authenticateAs(saleUser);
                TransactionRequest saleRequest = new TransactionRequest();
                saleRequest.setProductId(productId);
                saleRequest.setQuantity(SALE_QUANTITY);
                stockTransactionService.saleProduct(saleRequest);
                return null;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        boolean saleAcquiredLockWhileEditHeld;
        boolean saleAcquiredLockAfterRelease;
        try {
            Future<Void> editFuture = editExecutor.submit(editTask);

            if (!editLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                // The worker died before signalling; surface its cause rather than a bare timeout.
                editFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the edit thread never acquired the product row lock");
            }

            Future<Void> saleFuture = saleExecutor.submit(saleTask);

            if (!saleLockAttempted.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                saleFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                fail("the sale thread never reached its lock attempt");
            }

            // Negative proof: with the attempt already in flight, the lock must not be granted.
            saleAcquiredLockWhileEditHeld =
                    saleLockAcquired.await(NEGATIVE_CHECK_MILLIS, TimeUnit.MILLISECONDS);

            releaseEdit.countDown();

            editFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            saleFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            saleAcquiredLockAfterRelease =
                    saleLockAcquired.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            // A failed assertion must never leave the edit thread pinned or the pool alive.
            releaseEdit.countDown();
            editExecutor.shutdownNow();
            saleExecutor.shutdownNow();
            editExecutor.awaitTermination(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            saleExecutor.awaitTermination(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        return new ConcurrencyOutcome(
                saleAcquiredLockWhileEditHeld,
                saleAcquiredLockAfterRelease,
                productId,
                saleUserId,
                editedName,
                editedDescription,
                editedPrice);
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
            throw new IllegalStateException(
                    "the locking lookup returned no product for id " + productId);
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
        user.setUsername("concurrency_" + role + "_" + suffix);
        user.setEmail("concurrency_" + role + "_" + suffix + "@example.com");
        user.setPassword("not-used-in-this-test");
        user.setFullName("Concurrency " + role + " User " + suffix);
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Product persistProduct() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("concurrency-category-" + suffix);
        categoryId = categoryRepository.save(category).getId();

        Product product = new Product();
        product.setName("Concurrency Test Product " + suffix);
        product.setSku("CONC-SKU-" + suffix);
        product.setDescription("original description");
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(INITIAL_STOCK);
        product.setReorderLevel(5);
        product.setCategory(categoryRepository.findById(categoryId).orElseThrow());

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

    private record ConcurrencyOutcome(
            boolean saleAcquiredLockWhileEditHeld,
            boolean saleAcquiredLockAfterRelease,
            Long productId,
            Long saleUserId,
            String editedName,
            String editedDescription,
            BigDecimal editedPrice) {
    }
}
