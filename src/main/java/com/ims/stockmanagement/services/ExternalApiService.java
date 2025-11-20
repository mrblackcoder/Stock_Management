package com.ims.stockmanagement.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Harici API entegrasyonu servisi
 * Currency Exchange API kullanarak döviz kuru dönüşümü yapar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final RestTemplate restTemplate;

    private static final String EXCHANGE_API_URL = "https://api.exchangerate-api.com/v4/latest/";

    /**
     * Belirtilen para birimi için güncel döviz kurlarını getirir
     *
     * @param baseCurrency Ana para birimi (ör. "TRY", "USD", "EUR")
     * @return Döviz kurları map'i
     */
    public Map<String, Object> getExchangeRates(String baseCurrency) {
        try {
            String url = EXCHANGE_API_URL + baseCurrency;
            log.info("Fetching exchange rates for: {}", baseCurrency);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                log.info("Successfully fetched exchange rates for: {}", baseCurrency);
                return response;
            } else {
                log.warn("Empty response from exchange API");
                throw new RuntimeException("Failed to fetch exchange rates");
            }
        } catch (Exception e) {
            log.error("Error fetching exchange rates: {}", e.getMessage());
            throw new RuntimeException("External API error: " + e.getMessage());
        }
    }

    /**
     * Fiyatı bir para biriminden diğerine çevirir
     *
     * @param price Dönüştürülecek fiyat
     * @param fromCurrency Kaynak para birimi (ör. "TRY")
     * @param toCurrency Hedef para birimi (ör. "USD")
     * @return Dönüştürülmüş fiyat
     */
    public BigDecimal convertPrice(BigDecimal price, String fromCurrency, String toCurrency) {
        try {
            // Aynı para birimiyse direkt döndür
            if (fromCurrency.equalsIgnoreCase(toCurrency)) {
                return price;
            }

            Map<String, Object> exchangeData = getExchangeRates(fromCurrency);
            Map<String, Double> rates = (Map<String, Double>) exchangeData.get("rates");

            if (rates == null || !rates.containsKey(toCurrency)) {
                log.error("Exchange rate not found for currency: {}", toCurrency);
                throw new RuntimeException("Currency not supported: " + toCurrency);
            }

            Double rate = rates.get(toCurrency);
            BigDecimal convertedPrice = price.multiply(BigDecimal.valueOf(rate));

            log.info("Converted {} {} to {} {} (rate: {})",
                    price, fromCurrency, convertedPrice, toCurrency, rate);

            return convertedPrice.setScale(2, BigDecimal.ROUND_HALF_UP);

        } catch (Exception e) {
            log.error("Error converting price: {}", e.getMessage());
            throw new RuntimeException("Price conversion failed: " + e.getMessage());
        }
    }

    /**
     * Desteklenen para birimlerini listeler
     *
     * @return Para birimi listesi
     */
    public Map<String, Object> getSupportedCurrencies() {
        try {
            // USD bazlı kurları al (en çok desteklenen)
            Map<String, Object> data = getExchangeRates("USD");
            Map<String, Double> rates = (Map<String, Double>) data.get("rates");

            log.info("Total supported currencies: {}", rates.size());
            return Map.of(
                "base", "USD",
                "currencies", rates.keySet(),
                "count", rates.size()
            );
        } catch (Exception e) {
            log.error("Error fetching supported currencies: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch currencies");
        }
    }
}

