package com.casestudy.cryptoexchangeapi.exchange.service;

import com.casestudy.cryptoexchangeapi.base.AbstractBaseServiceTest;
import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.common.model.CustomSorting;
import com.casestudy.cryptoexchangeapi.exchange.exception.ConversionFailedException;
import com.casestudy.cryptoexchangeapi.exchange.feign.CmcClient;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoMapResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbol;
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

class CryptoConvertServiceTest extends AbstractBaseServiceTest {

    @Mock
    private CmcClient cmcClient;

    @Mock
    private CryptoConvertRepository cryptoConvertRepository;

    @InjectMocks
    private CryptoConvertService service;

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

    @Test
    void listCryptoNamesSymbols_fullPage_mapsItems_andComputesTotalPages_asPagePlus2() {
        // Given: page domain=1 -> zero-based = 0; size=2; start = 0*2 + 1 = 1
        CustomPagingRequest paging = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder().pageNumber(1).pageSize(2).build())
                .sorting(null)
                .build();

        CryptoMapResponse.Item i1 = new CryptoMapResponse.Item();
        i1.setName("Bitcoin"); i1.setSymbol("BTC");
        CryptoMapResponse.Item i2 = new CryptoMapResponse.Item();
        i2.setName("Ethereum"); i2.setSymbol("ETH");

        CryptoMapResponse resp = new CryptoMapResponse();
        resp.setData(List.of(i1, i2)); // content size == page size -> "full page"

        when(cmcClient.cryptoMap(1, 2, "cmc_rank")).thenReturn(resp);

        // When
        CustomPage<CryptoNameSymbol> page = service.listCryptoNamesSymbols(paging);

        // Then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Bitcoin");
        assertThat(page.getContent().get(0).getSymbol()).isEqualTo("BTC");
        assertThat(page.getContent().get(1).getName()).isEqualTo("Ethereum");
        assertThat(page.getContent().get(1).getSymbol()).isEqualTo("ETH");

        // page meta:
        assertThat(page.getPageNumber()).isEqualTo(1);    // domain page (zero-based+1)
        assertThat(page.getPageSize()).isEqualTo(2);
        assertThat(page.getTotalElementCount()).isEqualTo(2);
        assertThat(page.getTotalPageCount()).isEqualTo(2); // full page => page(0)+2

        verify(cmcClient, times(1)).cryptoMap(1, 2, "cmc_rank");
        verifyNoInteractions(cryptoConvertRepository);
        verifyNoMoreInteractions(cmcClient);
    }

    @Test
    void listCryptoNamesSymbols_shortPage_lastPage_totalPages_asPagePlus1() {

        // Given
        CustomPagingRequest paging = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder().pageNumber(2).pageSize(3).build())
                .sorting(null)
                .build();

        CryptoMapResponse.Item i1 = new CryptoMapResponse.Item();
        i1.setName("Arbitrum"); i1.setSymbol("ARB");

        CryptoMapResponse resp = new CryptoMapResponse();
        resp.setData(List.of(i1)); // short page (size 1 < 3)

        // When
        when(cmcClient.cryptoMap(4, 3, "cmc_rank")).thenReturn(resp);

        // Then
        CustomPage<CryptoNameSymbol> page = service.listCryptoNamesSymbols(paging);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getName()).isEqualTo("Arbitrum");
        assertThat(page.getContent().getFirst().getSymbol()).isEqualTo("ARB");

        assertThat(page.getPageNumber()).isEqualTo(2);
        assertThat(page.getPageSize()).isEqualTo(3);
        assertThat(page.getTotalElementCount()).isEqualTo(1);
        assertThat(page.getTotalPageCount()).isEqualTo(2);

        // Verify
        verify(cmcClient, times(1)).cryptoMap(4, 3, "cmc_rank");
        verifyNoInteractions(cryptoConvertRepository);
        verifyNoMoreInteractions(cmcClient);

    }

    @Test
    void listCryptoNamesSymbols_withNullPaging_usesDefaultPageable_andHandlesNullResponse() {

        // Given
        when(cmcClient.cryptoMap(1, 20, "cmc_rank")).thenReturn(null);

        // When
        CustomPage<CryptoNameSymbol> page = service.listCryptoNamesSymbols(null);

        // Then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getPageNumber()).isEqualTo(1); // zero-based(0)+1
        assertThat(page.getPageSize()).isEqualTo(20);
        assertThat(page.getTotalElementCount()).isEqualTo(0);
        assertThat(page.getTotalPageCount()).isEqualTo(1); // short page condition (0 < 20) => 0+1

        // Verify
        verify(cmcClient, times(1)).cryptoMap(1, 20, "cmc_rank");
        verifyNoInteractions(cryptoConvertRepository);
        verifyNoMoreInteractions(cmcClient);

    }

    @Test
    void listCryptoNamesSymbols_handlesEmptyDataList() {

        // Given
        CustomPagingRequest paging = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder().pageNumber(1).pageSize(5).build())
                .build();

        CryptoMapResponse resp = new CryptoMapResponse();
        resp.setData(List.of());

        //  When
        when(cmcClient.cryptoMap(1, 5, "cmc_rank")).thenReturn(resp);

        // Then
        CustomPage<CryptoNameSymbol> page = service.listCryptoNamesSymbols(paging);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getPageNumber()).isEqualTo(1);
        assertThat(page.getPageSize()).isEqualTo(5);
        assertThat(page.getTotalElementCount()).isEqualTo(0);
        assertThat(page.getTotalPageCount()).isEqualTo(1); // short page

        // Verify
        verify(cmcClient, times(1)).cryptoMap(1, 5, "cmc_rank");
        verifyNoInteractions(cryptoConvertRepository);
        verifyNoMoreInteractions(cmcClient);

    }

}
