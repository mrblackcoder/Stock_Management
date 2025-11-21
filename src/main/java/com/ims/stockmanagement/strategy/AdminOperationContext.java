package com.ims.stockmanagement.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Context class for managing admin operation strategies
 * Implements Strategy Pattern context
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationContext {

    private final List<AdminOperationStrategy> strategies;

    /**
     * Get strategy by operation name
     * @param operationName name of the operation
     * @return strategy instance
     */
    public AdminOperationStrategy getStrategy(String operationName) {
        return strategies.stream()
                .filter(strategy -> strategy.getOperationName().equals(operationName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + operationName));
    }

    /**
     * Execute strategy by operation name
     * @param operationName name of the operation
     * @param isAdmin whether the user is admin
     */
    public void executeStrategy(String operationName, boolean isAdmin) {
        AdminOperationStrategy strategy = getStrategy(operationName);

        if (strategy.requiresAdminRole() && !isAdmin) {
            throw new SecurityException("Admin role required for operation: " + operationName);
        }

        log.info("Executing strategy: {}", operationName);
        strategy.execute();
    }

    /**
     * Get all available strategies
     * @return map of strategy names to strategies
     */
    public Map<String, AdminOperationStrategy> getAllStrategies() {
        return strategies.stream()
                .collect(Collectors.toMap(
                        AdminOperationStrategy::getOperationName,
                        Function.identity()
                ));
    }

    /**
     * Get admin-only strategies
     * @return list of admin strategies
     */
    public List<AdminOperationStrategy> getAdminStrategies() {
        return strategies.stream()
                .filter(AdminOperationStrategy::requiresAdminRole)
                .collect(Collectors.toList());
    }

    /**
     * Get user-accessible strategies
     * @return list of user strategies
     */
    public List<AdminOperationStrategy> getUserStrategies() {
        return strategies.stream()
                .filter(strategy -> !strategy.requiresAdminRole())
                .collect(Collectors.toList());
    }
}

