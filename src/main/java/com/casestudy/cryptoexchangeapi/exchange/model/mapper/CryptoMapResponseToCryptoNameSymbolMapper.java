package com.casestudy.cryptoexchangeapi.exchange.model.mapper;

import com.casestudy.cryptoexchangeapi.common.model.mapper.BaseMapper;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoMapResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbol;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbolResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CryptoMapResponseToCryptoNameSymbolMapper extends BaseMapper<CryptoNameSymbolResponse, CryptoNameSymbol> {

    @Mapping(target = "name",   source = "name")
    @Mapping(target = "symbol", source = "symbol")
    CryptoNameSymbol map(CryptoMapResponse.Item source);

    static CryptoMapResponseToCryptoNameSymbolMapper initialize() {
        return Mappers.getMapper(CryptoMapResponseToCryptoNameSymbolMapper.class);
    }

}
