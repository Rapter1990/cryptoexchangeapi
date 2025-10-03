package com.casestudy.cryptoexchangeapi.exchange.model.mapper;

import com.casestudy.cryptoexchangeapi.common.model.mapper.BaseMapper;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbol;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbolResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CryptoNameSymbolToCryptoNameSymbolResponseMapper extends BaseMapper<CryptoNameSymbolResponse, CryptoNameSymbol> {

    @Mapping(target = "name",   source = "name")
    @Mapping(target = "symbol", source = "symbol")
    CryptoNameSymbolResponse map(CryptoNameSymbol source);

    static CryptoNameSymbolToCryptoNameSymbolResponseMapper initialize() {
        return Mappers.getMapper(CryptoNameSymbolToCryptoNameSymbolResponseMapper.class);
    }

}
