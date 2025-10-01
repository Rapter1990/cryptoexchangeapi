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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
public class CryptoConvertController {

    private final CryptoConvertService service;

    private static final CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper PAGE_MAPPER =
            CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper.initialize();

    @PostMapping
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

}
