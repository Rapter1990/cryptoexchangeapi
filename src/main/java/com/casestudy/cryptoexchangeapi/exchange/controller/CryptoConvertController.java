package com.casestudy.cryptoexchangeapi.exchange.controller;

import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
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

    @PostMapping
    public CustomResponse<CryptoConvert> convert(@Valid @RequestBody ConvertRequest req) {
        CryptoConvert savedCryptoConvert =
                service.convertAndPersist(req);
        return CustomResponse.createdOf(savedCryptoConvert);
    }
}
