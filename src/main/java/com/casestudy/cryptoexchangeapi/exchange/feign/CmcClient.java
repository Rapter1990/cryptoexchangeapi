package com.casestudy.cryptoexchangeapi.exchange.feign;

import com.casestudy.cryptoexchangeapi.exchange.config.CmcFeignConfig;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoMapResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.PriceConversionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "cmcClient",
        url = "${cmc.base-url}",
        configuration = CmcFeignConfig.class
)
public interface CmcClient {

    // /v1/tools/price-conversion?amount=...&symbol=...&convert=...
    @GetMapping("/v1/tools/price-conversion")
    PriceConversionResponse priceConversion(
            @RequestParam("amount") String amount,
            @RequestParam(value = "symbol", required = false) String fromSymbol,
            @RequestParam(value = "id", required = false) String fromId,
            @RequestParam(value = "convert", required = false) String toSymbol,
            @RequestParam(value = "convert_id", required = false) String toId);

    // (Optional) /v1/cryptocurrency/map
    @GetMapping("/v1/cryptocurrency/map")
    CryptoMapResponse cryptoMap(@RequestParam(value = "listing_status", defaultValue = "active") String status,
                                @RequestParam(value = "start", defaultValue = "1") int start,
                                @RequestParam(value = "limit", defaultValue = "5000") int limit);

}
