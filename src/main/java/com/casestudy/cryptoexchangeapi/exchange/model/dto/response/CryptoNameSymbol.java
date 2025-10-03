package com.casestudy.cryptoexchangeapi.exchange.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CryptoNameSymbol {

    private String name;
    private String symbol;

}
