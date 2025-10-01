package com.casestudy.cryptoexchangeapi.exchange.repository;

import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CryptoConvertRepositoryCustom {

    Page<CryptoConvertEntity> searchWithCriteria(ListCryptoConvertRequest.Filter filter, Pageable pageable);

}
