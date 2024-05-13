package com.techtask.superexchange.controllers;

import com.techtask.superexchange.exceptions.InvalidCurrencyConversionInputException;
import com.techtask.superexchange.services.CurrencyConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyConversionController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionController.class);

    private final CurrencyConversionService currencyConversionService;

    @Autowired
    public CurrencyConversionController(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("")
    public ResponseEntity getAvailableCurrencies() {
        try {
            List<String> convertedResult = currencyConversionService.getAllCurrencies();
            return ResponseEntity.ok(convertedResult);
        } catch (Exception e) {
            logger.error("Unable to get currencies due to: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/convert")
    public ResponseEntity convertCurrency(
            @RequestParam("sourceCurrency") String sourceCurrency,
            @RequestParam("targetCurrency") String targetCurrency,
            @RequestParam("amount") Double amount) {

        try {
            Double convertedResult = currencyConversionService.convertCurrency(sourceCurrency, targetCurrency, amount);
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
            currencyFormatter.setCurrency(java.util.Currency.getInstance(targetCurrency));
            String formattedCurrency = currencyFormatter.format(convertedResult);
            return ResponseEntity.ok(formattedCurrency);
        } catch (Exception e) {
            logger.error("Unable to convert currency due to: {}", e.getMessage());
            if (e.getClass().equals(InvalidCurrencyConversionInputException.class)) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.internalServerError().build();
        }
    }
}
