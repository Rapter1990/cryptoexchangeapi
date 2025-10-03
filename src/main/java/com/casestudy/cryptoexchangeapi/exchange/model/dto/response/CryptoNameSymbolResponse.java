package com.casestudy.cryptoexchangeapi.exchange.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoNameSymbolResponse {
    private String name;
    private String symbol;
}
