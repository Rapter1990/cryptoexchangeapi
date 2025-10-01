package com.casestudy.cryptoexchangeapi.exchange.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PriceConversionResponse {
    private Status status;
    private ConversionData data;

    @Getter
    @Setter
    public static class Status {
        private String timestamp;
        private int error_code;
        private String error_message;
    }

    @Getter
    @Setter
    public static class ConversionData {
        private Long id;
        private String symbol;
        private String name;
        private java.math.BigDecimal amount;
        private String last_updated;
        private Map<String, Quote> quote;
    }

    @Getter
    @Setter
    public static class Quote {
        private java.math.BigDecimal price;
        private String last_updated;
    }

}
