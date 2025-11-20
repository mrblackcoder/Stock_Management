package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.ExchangeRateDTO;
import com.ims.stockmanagement.dtos.ResponseDTO;
import com.ims.stockmanagement.services.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public ResponseEntity<ResponseDTO> getExchangeRates() {
        try {
            ExchangeRateDTO rates = exchangeRateService.getExchangeRates();
            return ResponseEntity.ok(new ResponseDTO(true, "Exchange rates retrieved successfully", rates));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false, "Failed to retrieve exchange rates: " + e.getMessage(), null)
            );
        }
    }

    /**
     * TRY'yi USD'ye çevirir
     * GET /api/exchange/convert/usd?amount=15000
     */
    @GetMapping("/convert/usd")
    public ResponseEntity<ResponseDTO> convertToUSD(@RequestParam BigDecimal amount) {
        try {
            BigDecimal converted = exchangeRateService.convertTRYtoUSD(amount);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", "USD");

            return ResponseEntity.ok(new ResponseDTO(true, "Successfully converted to USD", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false, "Conversion failed: " + e.getMessage(), null)
            );
        }
    }

    /**
     * TRY'yi EUR'ya çevirir
     * GET /api/exchange/convert/eur?amount=15000
     */
    @GetMapping("/convert/eur")
    public ResponseEntity<ResponseDTO> convertToEUR(@RequestParam BigDecimal amount) {
        try {
            BigDecimal converted = exchangeRateService.convertTRYtoEUR(amount);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", "EUR");

            return ResponseEntity.ok(new ResponseDTO(true, "Successfully converted to EUR", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false, "Conversion failed: " + e.getMessage(), null)
            );
        }
    }

    /**
     * TRY'yi GBP'ye çevirir
     * GET /api/exchange/convert/gbp?amount=15000
     */
    @GetMapping("/convert/gbp")
    public ResponseEntity<ResponseDTO> convertToGBP(@RequestParam BigDecimal amount) {
        try {
            BigDecimal converted = exchangeRateService.convertTRYtoGBP(amount);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", "GBP");

            return ResponseEntity.ok(new ResponseDTO(true, "Successfully converted to GBP", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false, "Conversion failed: " + e.getMessage(), null)
            );
        }
    }

    /**
     * TRY'yi belirtilen para birimine çevirir
     * GET /api/exchange/convert?amount=15000&currency=USD
     */
    @GetMapping("/convert")
    public ResponseEntity<ResponseDTO> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String currency) {
        try {
            BigDecimal converted = exchangeRateService.convertTRY(amount, currency);

            Map<String, Object> result = new HashMap<>();
            result.put("originalAmount", amount);
            result.put("originalCurrency", "TRY");
            result.put("convertedAmount", converted);
            result.put("targetCurrency", currency.toUpperCase());

            return ResponseEntity.ok(new ResponseDTO(true,
                "Successfully converted to " + currency.toUpperCase(), result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ResponseDTO(false, "Conversion failed: " + e.getMessage(), null)
            );
        }
    }
}

