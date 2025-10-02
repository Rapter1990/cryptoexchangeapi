package com.casestudy.cryptoexchangeapi.exchange.service;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.common.model.CustomSorting;
import com.casestudy.cryptoexchangeapi.exchange.exception.ConversionFailedException;
import com.casestudy.cryptoexchangeapi.exchange.feign.CmcClient;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.PriceConversionResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import com.casestudy.cryptoexchangeapi.exchange.repository.CryptoConvertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CryptoConvertServiceTest {

    @Mock
    private CmcClient cmcClient;

    @Mock
    private CryptoConvertRepository cryptoConvertRepository;

    @InjectMocks
    private CryptoConvertService service;

    // ---------- Helpers ----------

    private ConvertRequest makeConvertReq(BigDecimal amount, EnumCryptoCurrency from, EnumCryptoCurrency to) {
        ConvertRequest req = new ConvertRequest();
        req.setAmount(amount);
        req.setFrom(from);
        req.setTo(to);
        return req;
    }

    private PriceConversionResponse okResponseWithPrice(EnumCryptoCurrency to, BigDecimal unitPrice) {

        // status: success
        PriceConversionResponse.Status status = new PriceConversionResponse.Status();
        status.setError_code(0);
        status.setError_message(null);

        // data with quote map
        PriceConversionResponse.ConversionData data = new PriceConversionResponse.ConversionData();
        PriceConversionResponse.Quote quote = new PriceConversionResponse.Quote();
        quote.setPrice(unitPrice);
        data.setQuote(Map.of(to.name(), quote));

        // assemble response
        PriceConversionResponse resp = new PriceConversionResponse();
        resp.setStatus(status);
        resp.setData(data);
        return resp;

    }

    private PriceConversionResponse statusOnly(int errorCode, String msg) {
        PriceConversionResponse.Status status = new PriceConversionResponse.Status();
        status.setError_code(errorCode);
        status.setError_message(msg);

        PriceConversionResponse resp = new PriceConversionResponse();
        resp.setStatus(status);
        return resp;
    }

    // ---------- convertAndPersist tests ----------

    @Test
    void convertAndPersist_happyPath_savesEntity_andMaps() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("2.5"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);

        // CMC returns OK with a unitPrice
        BigDecimal unitPrice = new BigDecimal("1000");
        when(cmcClient.priceConversion("2.5", "BTC", null, "ARB", null))
                .thenReturn(okResponseWithPrice(EnumCryptoCurrency.ARB, unitPrice));

        // Repo saves and returns entity
        CryptoConvertEntity saved = CryptoConvertEntity.builder()
                .transactionId(UUID.randomUUID().toString())
                .amount(req.getAmount())
                .fromCurrency(req.getFrom())
                .toCurrency(req.getTo())
                .convertedAmount(unitPrice.multiply(req.getAmount()))
                .build();
        when(cryptoConvertRepository.save(any(CryptoConvertEntity.class))).thenReturn(saved);

        // When
        CryptoConvert out = service.convertAndPersist(req);

        // Then
        assertThat(out).isNotNull();
        assertThat(out.getFrom()).isEqualTo(req.getFrom());
        assertThat(out.getTo()).isEqualTo(req.getTo());
        assertThat(out.getAmount()).isEqualByComparingTo(req.getAmount());
        assertThat(out.getConvertedAmount()).isEqualByComparingTo(unitPrice.multiply(req.getAmount()));

        verify(cmcClient, times(1)).priceConversion("2.5", "BTC", null, "ARB", null);
        verify(cryptoConvertRepository, times(1)).save(any(CryptoConvertEntity.class));
        verifyNoMoreInteractions(cmcClient, cryptoConvertRepository);
    }

    @Test
    void convertAndPersist_whenResponseNull_throws() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("1"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);
        when(cmcClient.priceConversion("1", "BTC", null, "ARB", null)).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> service.convertAndPersist(req))
                .isInstanceOf(ConversionFailedException.class)
                .hasMessageContaining("Null response or missing status");

        verify(cmcClient, times(1)).priceConversion("1", "BTC", null, "ARB", null);
        verify(cryptoConvertRepository, never()).save(any());
    }

    @Test
    void convertAndPersist_whenStatusErrorCodeNonZero_throws() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("1"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);
        when(cmcClient.priceConversion("1", "BTC", null, "ARB", null))
                .thenReturn(statusOnly(409, "oops"));

        // When / Then
        assertThatThrownBy(() -> service.convertAndPersist(req))
                .isInstanceOf(ConversionFailedException.class)
                .hasMessageContaining("CMC error: oops");

        verify(cryptoConvertRepository, never()).save(any());
    }

    @Test
    void convertAndPersist_whenDataNull_throws() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("1"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);

        PriceConversionResponse r = statusOnly(0, null);
        // status ok, but data null
        when(cmcClient.priceConversion("1", "BTC", null, "ARB", null)).thenReturn(r);

        // When / Then
        assertThatThrownBy(() -> service.convertAndPersist(req))
                .isInstanceOf(ConversionFailedException.class)
                .hasMessageContaining("response has no data");

        verify(cryptoConvertRepository, never()).save(any());
    }

    @Test
    void convertAndPersist_whenMissingQuoteForTarget_throws() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("1"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);

        // Prepare response with a different quote (e.g., ETH)
        PriceConversionResponse ok = okResponseWithPrice(EnumCryptoCurrency.ETH, new BigDecimal("123"));
        when(cmcClient.priceConversion("1", "BTC", null, "ARB", null)).thenReturn(ok);

        // When / Then
        assertThatThrownBy(() -> service.convertAndPersist(req))
                .isInstanceOf(ConversionFailedException.class)
                .hasMessageContaining("No quote for ARB");

        verify(cryptoConvertRepository, never()).save(any());
    }

    @Test
    void convertAndPersist_whenUnitPriceNull_throws() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("1"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);

        PriceConversionResponse ok = okResponseWithPrice(EnumCryptoCurrency.ARB, null);
        when(cmcClient.priceConversion("1", "BTC", null, "ARB", null)).thenReturn(ok);

        // When / Then
        assertThatThrownBy(() -> service.convertAndPersist(req))
                .isInstanceOf(ConversionFailedException.class)
                .hasMessageContaining("quote has null price");

        verify(cryptoConvertRepository, never()).save(any());
    }

    // ---------- getHistory tests ----------

    @Test
    void getHistory_buildsPageableAndMapsItems() {
        // Given
        // paging: pageNumber is zero-based inside toPageable(); supply domain object to get page index 0
        CustomPaging paging = CustomPaging.builder()
                .pageNumber(1)  // domain "1" -> zero-based 0
                .pageSize(10)
                .build();

        CustomSorting sorting = CustomSorting.builder()
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        CustomPagingRequest pagingRequest = CustomPagingRequest.builder()
                .pagination(paging)
                .sorting(sorting)
                .build();

        ListCryptoConvertRequest.Filter filter = new ListCryptoConvertRequest.Filter();
        filter.setFrom(EnumCryptoCurrency.BTC);
        filter.setTo(EnumCryptoCurrency.ARB);
        ListCryptoConvertRequest req = new ListCryptoConvertRequest();
        req.setFilter(filter);

        CryptoConvertEntity e = CryptoConvertEntity.builder()
                .transactionId("tx-1")
                .amount(new BigDecimal("5"))
                .fromCurrency(EnumCryptoCurrency.BTC)
                .toCurrency(EnumCryptoCurrency.ARB)
                .convertedAmount(new BigDecimal("5000"))
                .build();

        Page<CryptoConvertEntity> repoPage = new PageImpl<>(List.of(e),
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt"))), 1);

        when(cryptoConvertRepository.searchWithCriteria(eq(filter), any(Pageable.class)))
                .thenReturn(repoPage);

        // When
        CustomPage<CryptoConvert> out = service.getHistory(req, pagingRequest);

        // Then
        assertThat(out).isNotNull();
        assertThat(out.getContent()).hasSize(1);
        CryptoConvert item = out.getContent().get(0);
        assertThat(item.getTransactionId()).isEqualTo("tx-1");
        assertThat(item.getFrom()).isEqualTo(EnumCryptoCurrency.BTC);
        assertThat(item.getTo()).isEqualTo(EnumCryptoCurrency.ARB);
        assertThat(item.getAmount()).isEqualByComparingTo("5");
        assertThat(item.getConvertedAmount()).isEqualByComparingTo("5000");

        // page meta should mirror repoPage (implementation-specific, so just sanity-check sizes)
        assertThat(out.getTotalElementCount()).isEqualTo(1);
        assertThat(out.getTotalPageCount()).isEqualTo(1);

        verify(cryptoConvertRepository, times(1)).searchWithCriteria(eq(filter), any(Pageable.class));
        verifyNoMoreInteractions(cryptoConvertRepository);
    }

    @Test
    void getHistory_withNullPaging_usesDefaultPageableAndMaps() {
        // Given
        ListCryptoConvertRequest req = new ListCryptoConvertRequest();
        req.setFilter(null); // no filters

        CryptoConvertEntity e = CryptoConvertEntity.builder()
                .transactionId("tx-2")
                .amount(new BigDecimal("1"))
                .fromCurrency(EnumCryptoCurrency.ETH)
                .toCurrency(EnumCryptoCurrency.BTC)
                .convertedAmount(new BigDecimal("0.05"))
                .build();

        Page<CryptoConvertEntity> repoPage = new PageImpl<>(List.of(e),
                PageRequest.of(1, 20, Sort.by(Sort.Order.desc("createdAt"))), 1);

        when(cryptoConvertRepository.searchWithCriteria(isNull(), any(Pageable.class)))
                .thenReturn(repoPage);

        // When
        CustomPage<CryptoConvert> out = service.getHistory(req, null);

        // Then
        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().get(0).getTransactionId()).isEqualTo("tx-2");

        verify(cryptoConvertRepository, times(1)).searchWithCriteria(isNull(), any(Pageable.class));
    }

    // ---------- fallbackConvertAndPersist ----------

    @Test
    void fallbackConvertAndPersist_alwaysThrowsWrapped() {
        // Given
        ConvertRequest req = makeConvertReq(new BigDecimal("1"), EnumCryptoCurrency.BTC, EnumCryptoCurrency.ARB);
        RuntimeException cause = new RuntimeException("io timeout");

        // When / Then
        assertThatThrownBy(() -> service.fallbackConvertAndPersist(req, cause))
                .isInstanceOf(ConversionFailedException.class)
                .hasMessageContaining("Upstream conversion unavailable")
                .hasCause(cause);
    }

}
