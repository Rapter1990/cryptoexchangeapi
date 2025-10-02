package com.casestudy.cryptoexchangeapi.exchange.config;

import com.casestudy.cryptoexchangeapi.exchange.utils.Constants; // adjust package if different
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringCacheCustomizerTest {

    private final SpringCacheCustomizer customizer = new SpringCacheCustomizer();

    @Test
    void customize_setsCacheNames_andDisallowsNullValues() {

        // Given
        ConcurrentMapCacheManager manager = new ConcurrentMapCacheManager();

        // When
        customizer.customize(manager);

        // Then
        assertThat(manager.getCacheNames())
                .containsExactly(Constants.EXCHANGE);
        assertThat(manager.isAllowNullValues()).isFalse();

    }

    @Test
    void customize_createsCache_andRejectsNullPuts() {

        // Given
        ConcurrentMapCacheManager manager = new ConcurrentMapCacheManager();
        customizer.customize(manager);

        // When
        Cache cache = manager.getCache(Constants.EXCHANGE);

        // Then
        assertThat(cache).isNotNull();

        cache.put("key", "value");
        assertThat(cache.get("key", String.class)).isEqualTo("value");

        // null puts are rejected when allowNullValues=false
        assertThatThrownBy(() -> cache.put("n", null))
                .isInstanceOf(IllegalArgumentException.class);

    }

}
