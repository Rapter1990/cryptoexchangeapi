package com.casestudy.cryptoexchangeapi.logging.repository;

import com.casestudy.cryptoexchangeapi.logging.model.entity.LogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<LogEntity,String> {

}
