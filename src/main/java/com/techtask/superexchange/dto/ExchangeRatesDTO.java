package com.techtask.superexchange.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExchangeRatesDTO {
    private String baseCurrency;
    private String quoteCurrency;
    private Double quote;
    private LocalDate date;
}
