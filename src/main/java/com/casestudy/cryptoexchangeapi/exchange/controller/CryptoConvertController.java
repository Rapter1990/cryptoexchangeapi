package com.casestudy.cryptoexchangeapi.exchange.controller;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomPagingResponse;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.FilterServicePagingRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoConvertResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.mapper.CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper;
import com.casestudy.cryptoexchangeapi.exchange.service.CryptoConvertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Crypto Convert API",
        description = "Convert between cryptocurrencies and query persisted conversion history. "
                + "POST /api/convert returns 201 with the saved conversion; "
                + "POST /api/convert/history supports filtering (from/to, amount & convertedAmount ranges, "
                + "createdAt range, transactionId substring) plus paging & sorting."
)
public class CryptoConvertController {

    private final CryptoConvertService service;

    private static final CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper PAGE_MAPPER =
            CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper.initialize();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<CryptoConvert> convert(@Valid @RequestBody ConvertRequest req) {

        CryptoConvert savedCryptoConvert = service.convertAndPersist(req);
        return CustomResponse.createdOf(savedCryptoConvert);

    }

    @PostMapping("/history")
    public CustomResponse<CustomPagingResponse<CryptoConvertResponse>> getHistory(
            @Valid @RequestBody FilterServicePagingRequest filterServicePagingRequest) {

        CustomPage<CryptoConvert> page = service.getHistory(
                filterServicePagingRequest.getFilterRequest(),
                filterServicePagingRequest.getPagingRequest()
        );

        CustomPagingResponse<CryptoConvertResponse> response = PAGE_MAPPER.toPagingResponse(page);
        return CustomResponse.successOf(response);

    }

    @CacheEvict(allEntries = true, cacheNames = {"exchanges"})
    @PostConstruct
    @Scheduled(fixedRateString = "${cmc.cache-ttl}")
    public void clearCache() {
        log.info("Caches are cleared");
    }

}
