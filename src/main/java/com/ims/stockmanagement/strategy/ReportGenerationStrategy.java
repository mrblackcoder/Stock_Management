package com.ims.stockmanagement.strategy;

import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy for generating reports
 * Accessible by both ADMIN and USER roles
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGenerationStrategy implements AdminOperationStrategy {

    private final ProductRepository productRepository;

    @Override
    public void execute() {
        log.info("Executing report generation operation");
    }

    @Override
    public String getOperationName() {
        return "REPORT_GENERATION";
    }

    @Override
    public boolean requiresAdminRole() {
        return false; // Users can also generate reports
    }

    @Override
    public String getDescription() {
        return "Generate inventory reports - Available for all users";
    }

    /**
     * Generate low stock report
     * @return list of products with low stock
     */
    public List<Product> generateLowStockReport() {
        log.info("Generating low stock report");
        return productRepository.findLowStockProducts();
    }

    /**
     * Generate inventory summary
     * @return summary statistics
     */
    public InventorySummary generateInventorySummary() {
        log.info("Generating inventory summary");
        List<Product> allProducts = productRepository.findAll();

        long totalProducts = allProducts.size();
        long lowStockProducts = productRepository.findLowStockProducts().size();
        int totalStock = allProducts.stream()
                .mapToInt(Product::getStockQuantity)
                .sum();

        return new InventorySummary(totalProducts, lowStockProducts, totalStock);
    }

    /**
     * Inner class for inventory summary data
     */
    public static class InventorySummary {
        public final long totalProducts;
        public final long lowStockProducts;
        public final int totalStock;

        public InventorySummary(long totalProducts, long lowStockProducts, int totalStock) {
            this.totalProducts = totalProducts;
            this.lowStockProducts = lowStockProducts;
            this.totalStock = totalStock;
        }
    }
}

