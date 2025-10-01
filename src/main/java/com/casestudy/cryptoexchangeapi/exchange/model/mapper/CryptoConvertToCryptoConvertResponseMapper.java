package com.casestudy.cryptoexchangeapi.exchange.model.mapper;

import com.casestudy.cryptoexchangeapi.common.model.mapper.BaseMapper;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoConvertResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CryptoConvertToCryptoConvertResponseMapper extends BaseMapper<CryptoConvertResponse, CryptoConvert> {

    CryptoConvertResponse map(CryptoConvert source);

    static CryptoConvertToCryptoConvertResponseMapper initialize() {
        return Mappers.getMapper(CryptoConvertToCryptoConvertResponseMapper.class);
    }

}
