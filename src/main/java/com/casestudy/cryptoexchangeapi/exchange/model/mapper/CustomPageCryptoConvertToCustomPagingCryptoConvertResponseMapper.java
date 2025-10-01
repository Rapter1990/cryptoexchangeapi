package com.casestudy.cryptoexchangeapi.exchange.model.mapper;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomPagingResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoConvertResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper {

    CryptoConvertToCryptoConvertResponseMapper ITEM_MAPPER =
            Mappers.getMapper(CryptoConvertToCryptoConvertResponseMapper.class);

    default CustomPagingResponse<CryptoConvertResponse> toPagingResponse(CustomPage<CryptoConvert> page) {
        if (page == null) return null;
        return CustomPagingResponse.<CryptoConvertResponse>builder()
                .content(toResponseList(page.getContent()))
                .totalElementCount(page.getTotalElementCount())
                .totalPageCount(page.getTotalPageCount())
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .build();
    }

    default List<CryptoConvertResponse> toResponseList(List<CryptoConvert> list) {
        if (list == null) return List.of();
        return list.stream().map(ITEM_MAPPER::map).toList();
    }

    static CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper initialize() {
        return Mappers.getMapper(CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper.class);
    }

}

