package com.casestudy.cryptoexchangeapi.exchange.config;

import com.casestudy.cryptoexchangeapi.base.AbstractBaseServiceTest;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CmcFeignConfigTest extends AbstractBaseServiceTest {

    @Mock
    private CmcProperties props;

    @Test
    void cmcKeyHeader_addsApiKeyAndAcceptHeaders() {

        // Given
        when(props.getApiKey()).thenReturn("TEST-KEY-123");
        CmcFeignConfig config = new CmcFeignConfig(props);
        RequestInterceptor interceptor = config.cmcKeyHeader();

        RequestTemplate template = new RequestTemplate();

        // When
        interceptor.apply(template);

        // Then
        Map<String, Collection<String>> headers = template.headers();

        assertThat(headers).containsKeys("X-CMC_PRO_API_KEY", "Accept");

        // exact header values
        assertThat(headers.get("X-CMC_PRO_API_KEY"))
                .singleElement()
                .isEqualTo("TEST-KEY-123");

        assertThat(headers.get("Accept"))
                .singleElement()
                .isEqualTo("application/json");
    }

    @Test
    void cmcKeyHeader_usesWhateverApiKeyIsProvidedByProps() {
        // Given
        when(props.getApiKey()).thenReturn("ANOTHER-KEY");
        CmcFeignConfig config = new CmcFeignConfig(props);
        RequestInterceptor interceptor = config.cmcKeyHeader();

        RequestTemplate template = new RequestTemplate();

        // When
        interceptor.apply(template);

        // Then
        assertThat(template.headers().get("X-CMC_PRO_API_KEY"))
                .singleElement()
                .isEqualTo("ANOTHER-KEY");

    }

}