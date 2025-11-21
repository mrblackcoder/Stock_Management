package com.ims.stockmanagement.strategy;

import com.ims.stockmanagement.models.Product;
import com.ims.stockmanagement.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy for bulk price update operations
 * Only accessible by ADMIN users
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BulkPriceUpdateStrategy implements AdminOperationStrategy {

    private final ProductRepository productRepository;

    @Override
    public void execute() {
        log.info("Executing bulk price update operation");
    }

    @Override
    public String getOperationName() {
        return "BULK_PRICE_UPDATE";
    }

    @Override
    public boolean requiresAdminRole() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Bulk update product prices - Admin only operation";
    }

    /**
     * Apply percentage increase to all products
     * @param percentage percentage to increase (e.g., 10 for 10%)
     */
    public void applyPercentageIncrease(double percentage) {
        log.info("Applying {}% price increase to all products", percentage);
        List<Product> products = productRepository.findAll();

        products.forEach(product -> {
            BigDecimal currentPrice = product.getPrice();
            BigDecimal increase = currentPrice.multiply(BigDecimal.valueOf(percentage / 100));
            BigDecimal newPrice = currentPrice.add(increase);
            product.setPrice(newPrice);
        });

        productRepository.saveAll(products);
        log.info("Successfully updated prices for {} products", products.size());
    }

    /**
     * Apply fixed amount increase to all products
     * @param amount amount to add to each product price
     */
    public void applyFixedIncrease(BigDecimal amount) {
        log.info("Applying fixed price increase of {} to all products", amount);
        List<Product> products = productRepository.findAll();

        products.forEach(product -> {
            BigDecimal newPrice = product.getPrice().add(amount);
            product.setPrice(newPrice);
        });

        productRepository.saveAll(products);
        log.info("Successfully updated prices for {} products", products.size());
    }
}

