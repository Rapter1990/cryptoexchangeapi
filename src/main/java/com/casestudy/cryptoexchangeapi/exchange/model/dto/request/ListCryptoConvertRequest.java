package com.casestudy.cryptoexchangeapi.exchange.model.dto.request;

import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ListCryptoConvertRequest {

    /**
     * Filter criteria for crypto-convert history search.
     */
    private Filter filter;

    @Getter
    @Setter
    public static class Filter {
        private EnumCryptoCurrency from;              // exact match
        private EnumCryptoCurrency to;                // exact match

        private BigDecimal minAmount;                 // amount >= min
        private BigDecimal maxAmount;                 // amount <= max

        private BigDecimal minConvertedAmount;        // convertedAmount >= min
        private BigDecimal maxConvertedAmount;        // convertedAmount <= max

        private LocalDateTime createdAtFrom;          // createdAt >= from
        private LocalDateTime createdAtTo;            // createdAt <= to

        private String transactionIdContains;         // case-insensitive contains
    }

}
