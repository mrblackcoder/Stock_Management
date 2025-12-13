package com.ims.stockmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Integration test - requires full configuration. Run manually.")
class StockManagementApplicationTests {

    @Test
    void contextLoads() {
    }

}
