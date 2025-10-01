package com.casestudy.cryptoexchangeapi.exchange.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CryptoMapResponse {

    private PriceConversionResponse.Status status;
    private List<Item> data;

    @Getter
    @Setter
    public static class Item {
        private Long id;
        private String name;
        private String symbol;
        private String slug;
        private Integer is_active;
    }

}
