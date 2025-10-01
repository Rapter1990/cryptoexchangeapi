package com.casestudy.cryptoexchangeapi.exchange.model.dto.request;

import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.validator.CryptoPairValid;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CryptoPairValid
public class ConvertRequest {

    @NotNull(message = "'from' is required")
    private EnumCryptoCurrency from;

    @NotNull(message = "'to' is required")
    private EnumCryptoCurrency to;

    @NotNull(message = "'amount' is required")
    @Positive(message = "'amount' must be > 0")
    private BigDecimal amount;

    @AssertTrue(message = "'from' and 'to' must be different")
    public boolean isDifferentPair() {
        return from == null || !from.equals(to);
    }

}
