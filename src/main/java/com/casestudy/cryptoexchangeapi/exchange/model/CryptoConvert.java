package com.casestudy.cryptoexchangeapi.exchange.model;

import com.casestudy.cryptoexchangeapi.common.model.BaseDomainModel;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CryptoConvert extends BaseDomainModel {

    private String transactionId;
    private BigDecimal amount;
    private EnumCryptoCurrency from;
    private EnumCryptoCurrency to;
    private BigDecimal convertedAmount;

}
