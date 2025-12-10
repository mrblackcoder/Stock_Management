package com.ims.stockmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StockManagementApplicationTests {

    @Test
    void contextLoads() {
        // Context load test with H2 in-memory database
    }

}
