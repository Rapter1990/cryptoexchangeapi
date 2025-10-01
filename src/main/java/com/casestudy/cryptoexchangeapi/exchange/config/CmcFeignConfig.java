package com.casestudy.cryptoexchangeapi.exchange.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CmcFeignConfig {

    private final CmcProperties props;

    @Bean
    public feign.RequestInterceptor cmcKeyHeader() {
        return template -> {
            template.header("X-CMC_PRO_API_KEY", props.getApiKey());
            template.header("Accept", "application/json");
        };
    }

}
