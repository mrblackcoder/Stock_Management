package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.TransactionDTO;
import com.ims.stockmanagement.dtos.TransactionRequest;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ownership rules for reading transaction history and for annotating a transaction.
 *
 * Each rule is asserted twice: once by calling StockTransactionService directly, which
 * proves the decision is made in the service rather than only at the HTTP route, and
 * once through the real filter chain with a real JWT, which proves the denial reaches
 * the caller as the documented 403 contract.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionAuthorizationIntegrationTest {

    private static final String ACCESS_FORBIDDEN_MESSAGE =
            "You do not have permission to access this resource.";
    private static final String ORIGINAL_NOTES = "original ledger note";
    private static final String NEW_NOTES = "annotated by the authorized caller";
    private static final String FORBIDDEN_NOTES = "written by someone who must not be allowed";

    @Autowired
    private StockTransactionService stockTransactionService;

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

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User otherUser;
    private User admin;
    private Product product;
    private StockTransaction transaction;

    @BeforeEach
    void setUp() {
        owner = saveUser("txn_owner", UserRole.USER);
        otherUser = saveUser("txn_other", UserRole.USER);
        admin = saveUser("txn_admin", UserRole.ADMIN);
        product = saveProduct();
        transaction = saveTransaction(owner, product);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ==================== Transaction history ====================

    @Test
    void userCanReadOwnTransactions() throws Exception {
        authenticateAs(owner);

        Response response = stockTransactionService.getTransactionsByUser(owner.getId());

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getTransactionList().stream()
                        .map(TransactionDTO::getId)
                        .anyMatch(id -> transaction.getId().equals(id)),
                "a user must see their own ledger rows");

        mockMvc.perform(get("/api/transactions/user/" + owner.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void userCannotReadAnotherUsersTransactions() throws Exception {
        authenticateAs(owner);

        assertThrows(AccessDeniedException.class,
                () -> stockTransactionService.getTransactionsByUser(otherUser.getId()),
                "a normal user must not read another user's history");

        mockMvc.perform(get("/api/transactions/user/" + otherUser.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(owner)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value(ACCESS_FORBIDDEN_MESSAGE))
                .andExpect(jsonPath("$.transactionList").doesNotExist());
    }

    @Test
    void adminCanReadAnotherUsersTransactions() throws Exception {
        authenticateAs(admin);

        Response response = stockTransactionService.getTransactionsByUser(owner.getId());

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getTransactionList().stream()
                        .map(TransactionDTO::getId)
                        .anyMatch(id -> transaction.getId().equals(id)),
                "an admin must be able to audit any user's history");

        mockMvc.perform(get("/api/transactions/user/" + owner.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    // ==================== Transaction notes ====================

    @Test
    void userCanUpdateOwnTransactionNotes() {
        authenticateAs(owner);
        TransactionSnapshot before = TransactionSnapshot.of(transaction);
        int stockBefore = product.getStockQuantity();

        Response response = stockTransactionService.updateTransaction(transaction.getId(), notesRequest(NEW_NOTES));

        assertEquals(200, response.getStatusCode());
        StockTransaction reloaded = reloadTransaction();
        assertEquals(NEW_NOTES, reloaded.getNotes());
        before.assertOnlyNotesChanged(reloaded);
        assertPersistedStockUnchanged(stockBefore);
    }

    @Test
    void userCannotUpdateAnotherUsersTransactionNotes() throws Exception {
        authenticateAs(otherUser);
        TransactionSnapshot before = TransactionSnapshot.of(transaction);

        assertThrows(AccessDeniedException.class,
                () -> stockTransactionService.updateTransaction(transaction.getId(), notesRequest(FORBIDDEN_NOTES)),
                "a normal user must not annotate a transaction they did not perform");

        StockTransaction reloaded = reloadTransaction();
        assertEquals(ORIGINAL_NOTES, reloaded.getNotes(), "the rejected note must not be persisted");
        before.assertOnlyNotesChanged(reloaded);

        mockMvc.perform(put("/api/transactions/" + transaction.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(otherUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":%d,\"transactionType\":\"PURCHASE\",\"quantity\":1,\"notes\":\"%s\"}"
                                .formatted(product.getId(), FORBIDDEN_NOTES)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value(ACCESS_FORBIDDEN_MESSAGE));

        assertEquals(ORIGINAL_NOTES, reloadTransaction().getNotes());
    }

    @Test
    void adminCanUpdateAnyTransactionNotes() {
        authenticateAs(admin);
        TransactionSnapshot before = TransactionSnapshot.of(transaction);
        int stockBefore = product.getStockQuantity();

        Response response = stockTransactionService.updateTransaction(transaction.getId(), notesRequest(NEW_NOTES));

        assertEquals(200, response.getStatusCode());
        StockTransaction reloaded = reloadTransaction();
        assertEquals(NEW_NOTES, reloaded.getNotes());
        before.assertOnlyNotesChanged(reloaded);
        assertPersistedStockUnchanged(stockBefore);
    }

    // ==================== Fixtures ====================

    /**
     * Every transaction field an annotation must never touch. Captured as values, not as
     * a reference to the managed entity, so a mutation cannot hide inside the snapshot.
     */
    private record TransactionSnapshot(Long actorId,
                                       Long productId,
                                       Integer quantity,
                                       TransactionType transactionType,
                                       TransactionStatus status,
                                       BigDecimal unitPrice,
                                       BigDecimal totalPrice,
                                       LocalDateTime transactionDate) {

        static TransactionSnapshot of(StockTransaction transaction) {
            return new TransactionSnapshot(
                    transaction.getUser().getId(),
                    transaction.getProduct().getId(),
                    transaction.getQuantity(),
                    transaction.getTransactionType(),
                    transaction.getStatus(),
                    transaction.getUnitPrice(),
                    transaction.getTotalPrice(),
                    transaction.getTransactionDate());
        }

        void assertOnlyNotesChanged(StockTransaction actual) {
            assertEquals(actorId, actual.getUser().getId(), "actor must be immutable");
            assertEquals(productId, actual.getProduct().getId(), "product must be immutable");
            assertEquals(quantity, actual.getQuantity(), "quantity must be immutable");
            assertEquals(transactionType, actual.getTransactionType(), "transaction type must be immutable");
            assertEquals(status, actual.getStatus(), "status must be immutable");
            assertEquals(0, unitPrice.compareTo(actual.getUnitPrice()), "unit price must be immutable");
            assertEquals(0, totalPrice.compareTo(actual.getTotalPrice()), "total price must be immutable");
            assertEquals(transactionDate, actual.getTransactionDate(), "transaction date must be immutable");
        }
    }

    private TransactionRequest notesRequest(String notes) {
        TransactionRequest request = new TransactionRequest();
        request.setProductId(product.getId());
        request.setTransactionType(TransactionType.PURCHASE);
        request.setQuantity(1);
        request.setNotes(notes);
        return request;
    }

    private StockTransaction reloadTransaction() {
        return transactionRepository.findById(transaction.getId()).orElseThrow();
    }

    private Product reloadProduct() {
        return productRepository.findById(product.getId()).orElseThrow();
    }

    /**
     * Proves stock really did not move, against the database rather than against memory.
     *
     * Inside one persistence context a repository lookup hands back the very instance the
     * service already held, so reading stock off it would compare an object with itself
     * and could never fail. Flushing writes any pending change out, clearing detaches
     * every managed instance, and the lookup that follows is a genuine row read.
     */
    private void assertPersistedStockUnchanged(int stockBefore) {
        entityManager.flush();
        entityManager.clear();

        assertEquals(stockBefore, reloadProduct().getStockQuantity(),
                "annotating a transaction must not move stock");
    }

    /**
     * Matches how JwtAuthenticationFilter populates the context at runtime: the persisted
     * User entity as principal, so the service resolves the same row it would in production.
     */
    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private User saveUser(String prefix, UserRole role) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(prefix + "_" + unique);
        user.setEmail(prefix + "_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth");
        user.setFullName("Transaction Authorization " + prefix);
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Product saveProduct() {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("txn-authz-category-" + unique);
        category.setDescription("Transaction authorization fixture");
        categoryRepository.save(category);

        Product saved = new Product();
        saved.setName("Transaction Authorization Product " + unique);
        saved.setSku("TXNAUTHZ-" + unique);
        saved.setPrice(new BigDecimal("100.00"));
        saved.setStockQuantity(10);
        saved.setReorderLevel(1);
        saved.setCategory(category);
        return productRepository.save(saved);
    }

    private StockTransaction saveTransaction(User actor, Product forProduct) {
        StockTransaction saved = new StockTransaction();
        saved.setProduct(forProduct);
        saved.setUser(actor);
        saved.setTransactionType(TransactionType.PURCHASE);
        saved.setQuantity(3);
        saved.setUnitPrice(new BigDecimal("100.00"));
        saved.setTotalPrice(new BigDecimal("300.00"));
        saved.setStatus(TransactionStatus.COMPLETED);
        saved.setNotes(ORIGINAL_NOTES);
        return transactionRepository.save(saved);
    }
}
