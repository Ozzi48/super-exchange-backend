package com.techtask.superexchange.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtask.superexchange.dto.ExchangeRatesDTO;
import com.techtask.superexchange.exceptions.ExchangeRatesException;
import com.techtask.superexchange.exceptions.InvalidCurrencyConversionInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyConversionService {
    private final HttpClient httpClient;
    private final String exchangeRateApiUrl;
    private final String swopApiKey;
    private final static String BASE_FETCH_CURRENCY = "EUR";
    private Map<LocalDate, List<ExchangeRatesDTO>> exchangeRateCache = new HashMap<>();

    @Autowired
    public CurrencyConversionService(@Value("${swop.api.url}") String exchangeRateApiUrl, @Value("${swop.api.key}") String swopApiKey) {
        this.httpClient = HttpClient.newBuilder().build();
        this.exchangeRateApiUrl = exchangeRateApiUrl;
        this.swopApiKey = swopApiKey;
    }

    @Cacheable("exchangeRates")
    public List<ExchangeRatesDTO> getAllExchangeRates() {
        LocalDate today = LocalDate.now();
        if (exchangeRateCache.containsKey(today)) {
            return exchangeRateCache.get(today);
        } else {
            List<ExchangeRatesDTO> exchangeRates = fetchExchangeRates();
            exchangeRateCache.put(today, exchangeRates);
            return exchangeRates;
        }
    }

    @Cacheable("exchangeRates")
    public List<String> getAllCurrencies() {
        List<ExchangeRatesDTO> exchangeRates = getAllExchangeRates();
        List<String> currenciesList = exchangeRates.stream()
                .filter(exchangeRate -> !"ALL".equals(exchangeRate.getQuoteCurrency()))
                .map(ExchangeRatesDTO::getQuoteCurrency)
                .distinct()
                .toList();
        return currenciesList;
    }

    public Double convertCurrency(String sourceCurrency, String targetCurrency, Double amount) {
        if (sourceCurrency.isBlank() || targetCurrency.isBlank() || amount == null) {
            throw new InvalidCurrencyConversionInputException("Missing one or more mandatory input parameters");
        }

        List<ExchangeRatesDTO> exchangeRates = getAllExchangeRates();
        List<String> availableCurrencies = getAllCurrencies();

        if (!availableCurrencies.contains(sourceCurrency)) {
            throw new InvalidCurrencyConversionInputException("Currency is not available: " + sourceCurrency);
        }
        if (!availableCurrencies.contains(targetCurrency)) {
            throw new InvalidCurrencyConversionInputException("Currency is not available: " + targetCurrency);
        }

        double fromRate = 1.0;
        double toRate = 1.0;
        if (!sourceCurrency.equals(BASE_FETCH_CURRENCY)) {
            for (ExchangeRatesDTO rate : exchangeRates) {
                if (rate.getQuoteCurrency().equals(sourceCurrency)) {
                    fromRate = rate.getQuote();
                    break;
                }
            }
        }

        if (!targetCurrency.equals(BASE_FETCH_CURRENCY)) {
            for (ExchangeRatesDTO rate : exchangeRates) {
                if (rate.getQuoteCurrency().equals(targetCurrency)) {
                    toRate = rate.getQuote();
                    break;
                }
            }
        }

        return amount * (toRate / fromRate);
    }

    public List<ExchangeRatesDTO> fetchExchangeRates() {
        String convertApiUrl = exchangeRateApiUrl + "/rates?base_currency=" + BASE_FETCH_CURRENCY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(convertApiUrl))
                .header("Authorization", "ApiKey " + swopApiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                try {
                    String responseBody = response.body();
                    List<ExchangeRatesDTO> exchangeRateResponse = new ObjectMapper().findAndRegisterModules().readValue(responseBody, new TypeReference<>() {});
                    return exchangeRateResponse;
                } catch (JsonProcessingException e) {
                    throw new ExchangeRatesException("Failed in parsing exchange rates response data, " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new ExchangeRatesException("Failed to fetch exchange rates data from the API" + e.getMessage());
        }
        throw new ExchangeRatesException("Failed to fetch exchange rate data from the API");
    }
}
