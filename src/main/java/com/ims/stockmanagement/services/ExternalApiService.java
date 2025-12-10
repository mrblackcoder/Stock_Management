package com.ims.stockmanagement.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * External API integration service
 * Performs currency exchange rate conversion using Currency Exchange API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final RestTemplate restTemplate;

    private static final String EXCHANGE_API_URL = "https://api.exchangerate-api.com/v4/latest/";

    /**
     * Fetches current exchange rates for the specified base currency
     *
     * @param baseCurrency Base currency code (e.g., "TRY", "USD", "EUR")
     * @return Exchange rates map
     * @throws IllegalArgumentException if currency code is invalid
     */
    public Map<String, Object> getExchangeRates(String baseCurrency) {
        if (baseCurrency == null || baseCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }

        String sanitizedCurrency = baseCurrency.toUpperCase().trim();
        if (!sanitizedCurrency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid currency code format: " + baseCurrency);
        }

        try {
            String url = EXCHANGE_API_URL + sanitizedCurrency;
            log.info("Fetching exchange rates for: {}", sanitizedCurrency);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.isEmpty()) {
                log.warn("Empty response from exchange API for currency: {}", sanitizedCurrency);
                throw new RuntimeException("Failed to fetch exchange rates: empty response");
            }

            log.info("Successfully fetched exchange rates for: {}", sanitizedCurrency);
            return response;
        } catch (RuntimeException e) {
            log.error("Error fetching exchange rates for {}: {}", baseCurrency, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching exchange rates for {}: {}", baseCurrency, e.getMessage());
            throw new RuntimeException("External API error: " + e.getMessage(), e);
        }
    }

    /**
     * Converts price from one currency to another
     *
     * @param price        Price to convert
     * @param fromCurrency Source currency code (e.g., "TRY")
     * @param toCurrency   Target currency code (e.g., "USD")
     * @return Converted price rounded to 2 decimal places
     * @throws IllegalArgumentException if parameters are invalid
     */
    public BigDecimal convertPrice(BigDecimal price, String fromCurrency, String toCurrency) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currency codes cannot be null");
        }

        // Return original price if same currency
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }

        try {
            Map<String, Object> exchangeData = getExchangeRates(fromCurrency);

            @SuppressWarnings("unchecked")
            Map<String, Object> rates = (Map<String, Object>) exchangeData.get("rates");

            if (rates == null) {
                log.error("No rates found in exchange API response");
                throw new RuntimeException("Exchange rates not available");
            }

            String targetCurrency = toCurrency.toUpperCase().trim();
            if (!rates.containsKey(targetCurrency)) {
                log.error("Exchange rate not found for currency: {}", targetCurrency);
                throw new IllegalArgumentException("Currency not supported: " + targetCurrency);
            }

            Object rateObj = rates.get(targetCurrency);
            Double rate;
            if (rateObj instanceof Number) {
                rate = ((Number) rateObj).doubleValue();
            } else {
                throw new RuntimeException("Invalid rate format for currency: " + targetCurrency);
            }

            if (rate == null || rate <= 0) {
                throw new RuntimeException("Invalid exchange rate for currency: " + targetCurrency);
            }

            BigDecimal convertedPrice = price.multiply(BigDecimal.valueOf(rate));

            log.info("Converted {} {} to {} {} (rate: {})",
                    price, fromCurrency, convertedPrice.setScale(2, RoundingMode.HALF_UP), toCurrency, rate);

            return convertedPrice.setScale(2, RoundingMode.HALF_UP);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error converting price from {} to {}: {}", fromCurrency, toCurrency, e.getMessage());
            throw new RuntimeException("Price conversion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Lists supported currencies
     *
     * @return Currency list information
     */
    public Map<String, Object> getSupportedCurrencies() {
        try {
            // Fetch USD-based rates (most widely supported)
            Map<String, Object> data = getExchangeRates("USD");

            @SuppressWarnings("unchecked")
            Map<String, Object> rates = (Map<String, Object>) data.get("rates");

            if (rates == null || rates.isEmpty()) {
                throw new RuntimeException("No currencies found in exchange API response");
            }

            log.info("Total supported currencies: {}", rates.size());
            return Map.of(
                    "base", "USD",
                    "currencies", rates.keySet(),
                    "count", rates.size()
            );
        } catch (Exception e) {
            log.error("Error fetching supported currencies: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch currencies: " + e.getMessage(), e);
        }
    }
}
