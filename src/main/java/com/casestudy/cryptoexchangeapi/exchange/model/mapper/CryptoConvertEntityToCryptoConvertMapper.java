package com.casestudy.cryptoexchangeapi.exchange.model.mapper;

import com.casestudy.cryptoexchangeapi.common.model.mapper.BaseMapper;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CryptoConvertEntityToCryptoConvertMapper extends BaseMapper<CryptoConvertEntity, CryptoConvert> {

    @Mapping(source = "fromCurrency", target = "from")
    @Mapping(source = "toCurrency",   target = "to")
    CryptoConvert map(CryptoConvertEntity source);

    static CryptoConvertEntityToCryptoConvertMapper initialize() {
        return Mappers.getMapper(CryptoConvertEntityToCryptoConvertMapper.class);
    }
}
