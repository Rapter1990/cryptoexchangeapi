package com.casestudy.cryptoexchangeapi.exchange.config;

import com.casestudy.cryptoexchangeapi.exchange.utils.Constants;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringCacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        cacheManager.setCacheNames(List.of(Constants.EXCHANGE));
        cacheManager.setAllowNullValues(false);
    }

}
