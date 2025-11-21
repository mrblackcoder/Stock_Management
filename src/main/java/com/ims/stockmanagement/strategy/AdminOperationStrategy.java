package com.ims.stockmanagement.strategy;

/**
 * Strategy interface for admin operations
 * Implements Strategy Pattern for extensible admin functionality
 */
public interface AdminOperationStrategy {
    /**
     * Execute the admin operation
     */
    void execute();

    /**
     * Get the name of the operation
     * @return operation name
     */
    String getOperationName();

    /**
     * Check if operation requires admin role
     * @return true if admin role is required
     */
    boolean requiresAdminRole();

    /**
     * Get operation description
     * @return description of the operation
     */
    default String getDescription() {
        return "Admin operation: " + getOperationName();
    }
}

