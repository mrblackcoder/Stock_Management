package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.ExchangeRateDTO;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.services.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@CrossOrigin(origins = "http://localhost:3000")
public class ExchangeRateController {

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * Tüm döviz kurlarını getirir
     * GET /api/exchange/rates
     */
    @GetMapping("/rates")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getExchangeRates() {
        try {
            ExchangeRateDTO rates = exchangeRateService.getExchangeRates();
            return ResponseEntity.ok(Response.builder()
                    .statusCode(200)
                    .message("Exchange rates retrieved successfully")
                    .data(rates)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Response.builder()
                    .statusCode(400)
                    .message("Failed to retrieve exchange rates: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * TRY'yi USD'ye çevirir
     * GET /api/exchange/convert/usd?amount=15000
     */
    @GetMapping("/convert/usd")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> convertToUSD(@RequestParam BigDecimal amount) {
        try {
            BigDecimal converted = exchangeRateService.convertTRYtoUSD(amount);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", "USD");

            return ResponseEntity.ok(Response.builder()
                    .statusCode(200)
                    .message("Successfully converted to USD")
                    .data(result)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Response.builder()
                    .statusCode(400)
                    .message("Conversion failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * TRY'yi EUR'ya çevirir
     * GET /api/exchange/convert/eur?amount=15000
     */
    @GetMapping("/convert/eur")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> convertToEUR(@RequestParam BigDecimal amount) {
        try {
            BigDecimal converted = exchangeRateService.convertTRYtoEUR(amount);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", "EUR");

            return ResponseEntity.ok(Response.builder()
                    .statusCode(200)
                    .message("Successfully converted to EUR")
                    .data(result)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Response.builder()
                    .statusCode(400)
                    .message("Conversion failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * TRY'yi GBP'ye çevirir
     * GET /api/exchange/convert/gbp?amount=15000
     */
    @GetMapping("/convert/gbp")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> convertToGBP(@RequestParam BigDecimal amount) {
        try {
            BigDecimal converted = exchangeRateService.convertTRYtoGBP(amount);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", "GBP");

            return ResponseEntity.ok(Response.builder()
                    .statusCode(200)
                    .message("Successfully converted to GBP")
                    .data(result)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Response.builder()
                    .statusCode(400)
                    .message("Conversion failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * TRY'yi belirtilen para birimine çevirir
     * GET /api/exchange/convert?amount=15000&currency=USD
     */
    @GetMapping("/convert")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String currency) {
        try {
            BigDecimal converted = exchangeRateService.convertTRY(amount, currency);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", currency.toUpperCase());

            return ResponseEntity.ok(Response.builder()
                    .statusCode(200)
                    .message("Successfully converted to " + currency.toUpperCase())
                    .data(result)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Response.builder()
                    .statusCode(400)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Response.builder()
                    .statusCode(400)
                    .message("Conversion failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}

