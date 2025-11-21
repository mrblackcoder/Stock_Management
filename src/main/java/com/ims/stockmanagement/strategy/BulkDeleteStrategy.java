package com.ims.stockmanagement.strategy;

import com.ims.stockmanagement.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy for bulk delete operations
 * Only accessible by ADMIN users
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BulkDeleteStrategy implements AdminOperationStrategy {

    private final ProductRepository productRepository;

    @Override
    public void execute() {
        log.info("Executing bulk delete operation");
        // Toplu silme işlemi - gerçek implementasyon burada yapılır
    }

    @Override
    public String getOperationName() {
        return "BULK_DELETE";
    }

    @Override
    public boolean requiresAdminRole() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Bulk delete products - Admin only operation";
    }

    /**
     * Bulk delete products by IDs
     * @param productIds list of product IDs to delete
     */
    public void bulkDeleteByIds(List<Long> productIds) {
        log.info("Bulk deleting {} products", productIds.size());
        productRepository.deleteAllById(productIds);
        log.info("Successfully deleted {} products", productIds.size());
    }
}

