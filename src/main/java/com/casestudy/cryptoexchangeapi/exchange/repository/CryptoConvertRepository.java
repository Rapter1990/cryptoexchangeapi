package com.casestudy.cryptoexchangeapi.exchange.repository;

import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CryptoConvertRepository extends MongoRepository<CryptoConvertEntity,String> {

}
