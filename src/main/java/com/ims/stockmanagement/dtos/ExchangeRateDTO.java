package com.ims.stockmanagement.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDTO {
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;
}

