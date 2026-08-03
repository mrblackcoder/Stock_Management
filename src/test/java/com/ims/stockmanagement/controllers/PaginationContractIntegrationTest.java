package com.ims.stockmanagement.controllers;

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
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the serialized shape of the two paginated list endpoints.
 *
 * Both used to carry the repository's Page object in `data`, which serialized the raw
 * Product and StockTransaction entities alongside the DTO list and depended on
 * PageImpl's JSON layout - a shape Spring Data does not treat as a contract. The
 * assertions below run against the real controller, service and database, so they
 * describe what a client actually receives rather than a hand-built Response.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaginationContractIntegrationTest {

    /** Fields that only exist on a serialized Page, never on this API's contract. */
    private static final List<String> PAGE_INTERNALS =
            List.of("\"content\"", "\"pageable\"", "\"numberOfElements\"", "\"unpaged\"", "\"sort\"");

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

    private String token;
    private Product product;

    @BeforeEach
    void setUp() {
        User user = saveUser();
        token = jwtService.generateToken(user);
        product = saveProduct();
        saveTransaction(user, product);
    }

    @Test
    void productListReturnsStablePaginationMetadataWithoutRawEntityPage() throws Exception {
        String body = mockMvc.perform(get("/api/products?page=0&size=10&sortBy=createdAt")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
                // DTO projection, not entities: sku and categoryName only exist on ProductDTO.
                .andExpect(jsonPath("$.productList").isArray())
                .andExpect(jsonPath("$.productList[0].id").exists())
                .andExpect(jsonPath("$.productList[0].sku").exists())
                .andExpect(jsonPath("$.productList[0].categoryName").exists())
                // Stable, explicit pagination metadata at the top level.
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                // The raw Page is gone.
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertNoSerializedPage(body);
    }

    @Test
    void transactionListReturnsStablePaginationMetadataWithoutRawEntityPage() throws Exception {
        String body = mockMvc.perform(get("/api/transactions?page=0&size=10&sortBy=transactionDate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Transactions retrieved successfully"))
                // productName/username are TransactionDTO projections; the entity hides both.
                .andExpect(jsonPath("$.transactionList").isArray())
                .andExpect(jsonPath("$.transactionList[0].id").exists())
                .andExpect(jsonPath("$.transactionList[0].productName").exists())
                .andExpect(jsonPath("$.transactionList[0].username").exists())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertNoSerializedPage(body);
    }

    private void assertNoSerializedPage(String body) {
        for (String marker : PAGE_INTERNALS) {
            assertFalse(body.contains(marker),
                    "response must not expose serialized Page internals, found " + marker + " in: " + body);
        }
    }

    private User saveUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername("pagination_" + unique);
        user.setEmail("pagination_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth");
        user.setFullName("Pagination Contract User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Product saveProduct() {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        Category category = new Category();
        category.setName("pagination-category-" + unique);
        category.setDescription("Pagination contract fixture");
        categoryRepository.save(category);

        Product saved = new Product();
        saved.setName("Pagination Contract Product " + unique);
        saved.setSku("PAGE-" + unique);
        saved.setPrice(new BigDecimal("100.00"));
        saved.setStockQuantity(10);
        saved.setReorderLevel(1);
        saved.setCategory(category);
        return productRepository.save(saved);
    }

    private void saveTransaction(User actor, Product forProduct) {
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(forProduct);
        transaction.setUser(actor);
        transaction.setTransactionType(TransactionType.PURCHASE);
        transaction.setQuantity(2);
        transaction.setUnitPrice(new BigDecimal("100.00"));
        transaction.setTotalPrice(new BigDecimal("200.00"));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
    }
}
