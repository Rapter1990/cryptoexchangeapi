package com.casestudy.cryptoexchangeapi.exchange.config;

import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import com.casestudy.cryptoexchangeapi.common.model.CustomSorting;
import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryKeyConfigTest {

    private KeyGenerator keyGenerator;

    @BeforeEach
    void setUp() {
        keyGenerator = new HistoryKeyConfig().historyKeyGenerator();
    }

    @Test
    void withFullFilter_andCustomPagingRequest_buildsDeterministicKey() {
        // Given: filter payload
        ListCryptoConvertRequest.Filter f = new ListCryptoConvertRequest.Filter();
        f.setFrom(EnumCryptoCurrency.BTC);
        f.setTo(EnumCryptoCurrency.ARB);
        f.setMinAmount(new BigDecimal("50"));
        f.setMaxAmount(new BigDecimal("5000"));
        f.setMinConvertedAmount(new BigDecimal("1000000"));
        f.setMaxConvertedAmount(new BigDecimal("3000000000"));
        f.setCreatedAtFrom(LocalDateTime.of(2025, 9, 29, 0, 0, 0));
        f.setCreatedAtTo(LocalDateTime.of(2025, 10, 2, 23, 59, 59));
        f.setTransactionIdContains("6c7de4");

        ListCryptoConvertRequest req = new ListCryptoConvertRequest();
        req.setFilter(f);

        // And: paging via CustomPagingRequest (pageNumber is 1-based in your model)
        CustomPagingRequest paging = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder()
                        .pageNumber(2)  // -> zero-based page index becomes 1 inside toPageable()
                        .pageSize(25)
                        .build())
                .sorting(CustomSorting.builder()
                        .sortBy("createdAt")
                        .sortDirection("DESC")
                        .build())
                .build();

        // When
        Object key = keyGenerator.generate(new Object(), dummyMethod(), new Object[]{req, paging});
        String ks = key.toString();

        // Then: assert pieces are present (don’t rely on timezone-sensitive formatting elsewhere)
        assertThat(ks).startsWith("history::");
        assertThat(ks).contains("from=BTC");
        assertThat(ks).contains("to=ARB");
        assertThat(ks).contains("minAmt=50");
        assertThat(ks).contains("maxAmt=5000");
        assertThat(ks).contains("minConv=1000000");
        assertThat(ks).contains("maxConv=3000000000");
        assertThat(ks).contains("fromDate=2025-09-29T00:00");
        assertThat(ks).contains("toDate=2025-10-02T23:59:59");
        assertThat(ks).contains("txPart=6c7de4");
        // pageable derived from CustomPagingRequest: pageNumber 2 -> zero-based 1
        assertThat(ks).contains("|page=1|size=25");
        // sort serialization includes trailing comma per implementation
        assertThat(ks).contains("|sort=createdAt:DESC,");
    }

    @Test
    void withNullFilter_andNoPagingParam_usesDefaults() {
        // Given
        ListCryptoConvertRequest req = new ListCryptoConvertRequest();
        req.setFilter(null); // no filter

        // When (no second param -> defaults: page=0,size=20,sort=createdAt:DESC)
        Object key = keyGenerator.generate(new Object(), dummyMethod(), new Object[]{req});
        String ks = key.toString();

        // Then
        assertThat(ks).startsWith("history::nofilter");
        assertThat(ks).contains("|page=0|size=20");
        assertThat(ks).contains("|sort=createdAt:DESC,");
    }

    @Test
    void withDirectPageableParam_unsorted_reflectedInKey() {
        // Given: minimal filter (just to ensure filter branch is taken)
        ListCryptoConvertRequest.Filter f = new ListCryptoConvertRequest.Filter();
        f.setFrom(EnumCryptoCurrency.ETH);
        ListCryptoConvertRequest req = new ListCryptoConvertRequest();
        req.setFilter(f);

        Pageable pageable = PageRequest.of(3, 10, Sort.unsorted()); // zero-based page=3, size=10

        // When
        Object key = keyGenerator.generate(new Object(), dummyMethod(), new Object[]{req, pageable});
        String ks = key.toString();

        // Then
        assertThat(ks).contains("from=ETH");
        assertThat(ks).contains("to="); // empty
        assertThat(ks).contains("minAmt=");
        assertThat(ks).contains("maxAmt=");
        assertThat(ks).contains("minConv=");
        assertThat(ks).contains("maxConv=");
        assertThat(ks).contains("fromDate=");
        assertThat(ks).contains("toDate=");
        assertThat(ks).contains("txPart=");
        assertThat(ks).contains("|page=3|size=10");
        // unsorted -> sort section present but empty after '='
        assertThat(ks).contains("|sort=");
        // and should NOT contain createdAt:DESC in this case
        assertThat(ks).doesNotContain("createdAt:DESC");
    }

    /** Dummy reflect Method placeholder; keyGenerator doesn’t actually use the method metadata. */
    private java.lang.reflect.Method dummyMethod() {
        try {
            return this.getClass().getDeclaredMethod("withFullFilter_andCustomPagingRequest_buildsDeterministicKey_dummy");
        } catch (NoSuchMethodException e) {
            // Fallback to any method on Object
            try {
                return Object.class.getMethod("toString");
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
