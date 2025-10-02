package com.casestudy.cryptoexchangeapi.exchange.controller;

import com.casestudy.cryptoexchangeapi.base.AbstractRestControllerTest;
import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import com.casestudy.cryptoexchangeapi.common.model.CustomSorting;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomPagingResponse;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.FilterServicePagingRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoConvertResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import com.casestudy.cryptoexchangeapi.exchange.model.mapper.CryptoConvertToCryptoConvertResponseMapper;
import com.casestudy.cryptoexchangeapi.exchange.model.mapper.CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper;
import com.casestudy.cryptoexchangeapi.exchange.service.CryptoConvertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller integration-style tests using MockMvc + Mockito (no ArgumentCaptor).
 * Matches the style of the provided GithubScreenshotControllerTest.
 */
class CryptoConvertControllerTest extends AbstractRestControllerTest {

    @MockitoBean
    private CryptoConvertService service;

    private static final String BASE_URL = "/api/convert";

    private static final CryptoConvertToCryptoConvertResponseMapper DOMAIN_TO_RESPONSE =
            CryptoConvertToCryptoConvertResponseMapper.initialize();

    private static final CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper PAGE_MAPPER =
            CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper.initialize();

    // ------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------

    @Test
    @DisplayName("POST /api/convert -> 201 Created; delegates to service.convertAndPersist and returns payload")
    void create_HappyPath_Returns201() throws Exception {
        // Given
        ConvertRequest request = ConvertRequest.builder()
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ARB)
                .amount(new BigDecimal("100"))
                .build();

        CryptoConvert domain = sampleDomain();

        CryptoConvertResponse expectedResponse = DOMAIN_TO_RESPONSE.map(domain);

        // When
        when(service.convertAndPersist(any())).thenReturn(domain);

        // Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value("CREATED"))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.response.transactionId").value(expectedResponse.getTransactionId()))
                .andExpect(jsonPath("$.response.from").value(expectedResponse.getFrom().name()))
                .andExpect(jsonPath("$.response.to").value(expectedResponse.getTo().name()))
                .andExpect(jsonPath("$.response.amount").value(expectedResponse.getAmount().intValue()))
                .andExpect(jsonPath("$.response.convertedAmount").value(expectedResponse.getConvertedAmount().doubleValue()));

        verify(service).convertAndPersist(any());
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/convert -> 400 when from==to; service not invoked")
    void create_ValidationFailure_SamePair_Returns400_AndServiceNotCalled() throws Exception {
        // Given (from == to)
        ConvertRequest request = ConvertRequest.builder()
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.BTC)
                .amount(new BigDecimal("10"))
                .build();

        // Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message", anyOf(
                        is("Validation failed"),
                        notNullValue()
                )))
                .andExpect(jsonPath("$.subErrors", notNullValue()));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /api/convert -> 400 when amount is missing; service not invoked")
    void create_ValidationFailure_MissingAmount_Returns400_AndServiceNotCalled() throws Exception {
        // Given (amount missing)
        ConvertRequest request = ConvertRequest.builder()
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ARB)
                .amount(null)
                .build();

        // Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.subErrors", notNullValue()));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /api/convert -> 400 when amount is negative; service not invoked")
    void create_ValidationFailure_NegativeAmount_Returns400_AndServiceNotCalled() throws Exception {
        // Given (amount negative)
        ConvertRequest request = ConvertRequest.builder()
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ARB)
                .amount(new BigDecimal("-1"))
                .build();

        // Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.subErrors", notNullValue()));

        verifyNoInteractions(service);
    }

    // ------------------------------------------------------------
    // HISTORY
    // ------------------------------------------------------------

    @Test
    @DisplayName("POST /api/convert/history -> 200 OK; delegates to service.getHistory and returns page")
    void history_Paged_Returns200() throws Exception {
        // Given filter
        ListCryptoConvertRequest.Filter f = new ListCryptoConvertRequest.Filter();
        f.setFrom(EnumCryptoCurrency.BTC);
        f.setTo(EnumCryptoCurrency.ARB);
        f.setMinAmount(new BigDecimal("50"));
        f.setMaxAmount(new BigDecimal("5000"));
        f.setMinConvertedAmount(new BigDecimal("1000000"));
        f.setMaxConvertedAmount(new BigDecimal("4000000000"));
        f.setCreatedAtFrom(LocalDateTime.parse("2025-09-29T00:00:00"));
        f.setCreatedAtTo(LocalDateTime.parse("2025-10-02T23:59:59"));
        f.setTransactionIdContains("6c7de4");

        ListCryptoConvertRequest filterReq = new ListCryptoConvertRequest();
        filterReq.setFilter(f);

        // Given paging (your CustomPaging#getPageNumber returns zero-based internally)
        CustomPaging paging = CustomPaging.builder()
                .pageNumber(1)
                .pageSize(20)
                .build();
        CustomSorting sorting = CustomSorting.builder()
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
        CustomPagingRequest pagingReq = CustomPagingRequest.builder()
                .pagination(paging)
                .sorting(sorting)
                .build();

        FilterServicePagingRequest wrapper = FilterServicePagingRequest.builder()
                .filterRequest(filterReq)
                .pagingRequest(pagingReq)
                .build();

        CryptoConvert item = sampleDomain();

        @SuppressWarnings("unchecked")
        CustomPage<CryptoConvert> page = CustomPage.<CryptoConvert>builder()
                .content(List.of(item))
                .pageNumber(1)
                .pageSize(20)
                .totalElementCount(1L)
                .totalPageCount(1)
                .build();

        when(service.getHistory(any(), any())).thenReturn(page);

        // For completeness compute expected response envelope
        CustomPagingResponse<CryptoConvertResponse> expected = PAGE_MAPPER.toPagingResponse(page);
        CustomResponse<CustomPagingResponse<CryptoConvertResponse>> expectedEnvelope = CustomResponse.successOf(expected);

        // Then
        mockMvc.perform(post(BASE_URL + "/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.response.totalElementCount").value(expected.getTotalElementCount()))
                .andExpect(jsonPath("$.response.pageNumber").value(expected.getPageNumber()))
                .andExpect(jsonPath("$.response.pageSize").value(expected.getPageSize()))
                .andExpect(jsonPath("$.response.totalPageCount").value(expected.getTotalPageCount()))
                .andExpect(jsonPath("$.response.content", hasSize(1)))
                .andExpect(jsonPath("$.response.content[0].transactionId").value(item.getTransactionId()))
                .andExpect(jsonPath("$.response.content[0].from").value(item.getFrom().name()))
                .andExpect(jsonPath("$.response.content[0].to").value(item.getTo().name()))
                .andExpect(jsonPath("$.response.content[0].amount").value(item.getAmount().intValue()))
                .andExpect(jsonPath("$.response.content[0].convertedAmount").value(item.getConvertedAmount().doubleValue()));

        verify(service).getHistory(any(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /api/convert/history -> 400 when pagination is missing; service not invoked")
    void history_ValidationFailure_MissingPagination_Returns400_AndServiceNotCalled() throws Exception {
        // Given filter present, pagination is null inside pagingRequest
        ListCryptoConvertRequest filterReq = new ListCryptoConvertRequest();
        filterReq.setFilter(new ListCryptoConvertRequest.Filter());

        CustomPagingRequest badPaging = CustomPagingRequest.builder()
                .pagination(null) // @NotNull violated
                .sorting(null)
                .build();

        FilterServicePagingRequest wrapper = FilterServicePagingRequest.builder()
                .filterRequest(filterReq)
                .pagingRequest(badPaging)
                .build();

        mockMvc.perform(post(BASE_URL + "/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.header", containsStringIgnoringCase("VALIDATION")))
                .andExpect(jsonPath("$.subErrors", notNullValue()));

        verifyNoInteractions(service);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private CryptoConvert sampleDomain() {
        return CryptoConvert.builder()
                .transactionId("6c7de41f-71e5-4d63-984d-8dcb60ba6265")
                .amount(new BigDecimal("100"))
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ARB)
                .convertedAmount(new BigDecimal("2711598539.488985400"))
                .createdAt(LocalDateTime.of(2025, 10, 1, 18, 4, 33, 282_000_000))
                .build();
    }
}
