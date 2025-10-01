package com.casestudy.cryptoexchangeapi.exchange.model.dto.request;

import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import jakarta.validation.Valid;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterServicePagingRequest {

    @Valid
    private ListCryptoConvertRequest filterRequest;

    @Valid
    private CustomPagingRequest pagingRequest;
}
