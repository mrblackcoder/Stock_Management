package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.ExchangeRateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/TRY";
    private final RestTemplate restTemplate;

    /**
     * Dış API'den güncel döviz kurlarını çeker
     * @return ExchangeRateDTO - Tüm döviz kurları
     */
    public ExchangeRateDTO getExchangeRates() {
        try {
            ExchangeRateDTO rates = restTemplate.getForObject(API_URL, ExchangeRateDTO.class);
            if (rates == null || rates.getRates() == null) {
                throw new IllegalStateException("Invalid response from exchange rate API");
            }
            return rates;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Exchange rate API connection failed: {}", e.getMessage());
            throw new IllegalStateException("Exchange rate service is unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Failed to fetch exchange rates: {}", e.getMessage());
            throw new IllegalStateException("Failed to fetch exchange rates: " + e.getMessage());
        }
    }

    /**
     * TRY'yi USD'ye çevirir
     * @param amountInTRY - TRY cinsinden tutar
     * @return BigDecimal - USD cinsinden tutar
     */
    public BigDecimal convertTRYtoUSD(BigDecimal amountInTRY) {
        ExchangeRateDTO rates = getExchangeRates();
        BigDecimal usdRate = rates.getRates().get("USD");
        return amountInTRY.multiply(usdRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * TRY'yi EUR'ya çevirir
     * @param amountInTRY - TRY cinsinden tutar
     * @return BigDecimal - EUR cinsinden tutar
     */
    public BigDecimal convertTRYtoEUR(BigDecimal amountInTRY) {
        ExchangeRateDTO rates = getExchangeRates();
        BigDecimal eurRate = rates.getRates().get("EUR");
        return amountInTRY.multiply(eurRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * TRY'yi GBP'ye çevirir
     * @param amountInTRY - TRY cinsinden tutar
     * @return BigDecimal - GBP cinsinden tutar
     */
    public BigDecimal convertTRYtoGBP(BigDecimal amountInTRY) {
        ExchangeRateDTO rates = getExchangeRates();
        BigDecimal gbpRate = rates.getRates().get("GBP");
        return amountInTRY.multiply(gbpRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * TRY'yi belirtilen para birimine çevirir
     * @param amountInTRY - TRY cinsinden tutar
     * @param targetCurrency - Hedef para birimi (USD, EUR, GBP vs.)
     * @return BigDecimal - Hedef para biriminde tutar
     */
    public BigDecimal convertTRY(BigDecimal amountInTRY, String targetCurrency) {
        ExchangeRateDTO rates = getExchangeRates();
        BigDecimal rate = rates.getRates().get(targetCurrency.toUpperCase());

        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + targetCurrency);
        }

        return amountInTRY.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}

