package com.casestudy.cryptoexchangeapi.exchange.model.dto.response;

import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoConvertResponse {
    private String transactionId;
    private BigDecimal amount;
    private EnumCryptoCurrency from;
    private EnumCryptoCurrency to;
    private BigDecimal convertedAmount;
    private LocalDateTime createdAt;
}
