package com.casestudy.cryptoexchangeapi.exchange.service;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.exchange.exception.ConversionFailedException;
import com.casestudy.cryptoexchangeapi.exchange.feign.CmcClient;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.PriceConversionResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import com.casestudy.cryptoexchangeapi.exchange.model.mapper.CryptoConvertEntityToCryptoConvertMapper;
import com.casestudy.cryptoexchangeapi.exchange.repository.CryptoConvertRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CryptoConvertService {

    private final CmcClient cmcClient;
    private final CryptoConvertRepository cryptoConvertRepository;

    private final CryptoConvertEntityToCryptoConvertMapper mapper =
            CryptoConvertEntityToCryptoConvertMapper.initialize();

    @RateLimiter(name = "cmc")
    @Retry(name = "cmc")
    @CircuitBreaker(name = "cmc", fallbackMethod = "fallbackConvertAndPersist")
    @Transactional
    public CryptoConvert convertAndPersist(ConvertRequest request) {

        PriceConversionResponse response = cmcClient.priceConversion(
                request.getAmount().stripTrailingZeros().toPlainString(),
                request.getFrom().name(), null,
                request.getTo().name(), null
        );

        if (response == null || response.getStatus() == null) {
            throw new ConversionFailedException("Null response or missing status from CMC");
        }

        if (response.getStatus().getError_code() != 0) {
            String msg = response.getStatus().getError_message();
            throw new ConversionFailedException("CMC error: " + (msg == null ? "unknown" : msg));
        }

        if (response.getData() == null) {
            throw new ConversionFailedException("CMC response has no data");
        }

        Map<String, PriceConversionResponse.Quote> quoteMap = response.getData().getQuote();
        if (quoteMap == null || !quoteMap.containsKey(request.getTo().name())) {
            throw new ConversionFailedException("No quote for " + request.getTo().name() + " in CMC response");
        }

        PriceConversionResponse.Quote quote = quoteMap.get(request.getTo().name());
        BigDecimal unitPrice = quote.getPrice();
        if (unitPrice == null) {
            throw new ConversionFailedException("CMC quote has null price for " + request.getTo().name());
        }

        BigDecimal convertedAmount = unitPrice.multiply(request.getAmount());

        CryptoConvertEntity entity = CryptoConvertEntity.builder()
                .transactionId(UUID.randomUUID().toString())
                .amount(request.getAmount())
                .fromCurrency(request.getFrom())
                .toCurrency(request.getTo())
                .convertedAmount(convertedAmount)
                .build();

        CryptoConvertEntity saved = cryptoConvertRepository.save(entity);

        return mapper.map(saved);

    }

    @Transactional(readOnly = true)
    public CustomPage<CryptoConvert> getHistory(ListCryptoConvertRequest request,
                                                CustomPagingRequest pagingRequest) {

        Pageable pageable = Optional.ofNullable(pagingRequest)
                .map(CustomPagingRequest::toPageable)
                .orElse(PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        ListCryptoConvertRequest.Filter filter = Optional.ofNullable(request)
                .map(ListCryptoConvertRequest::getFilter)
                .orElse(null);

        Page<CryptoConvertEntity> page = cryptoConvertRepository.searchWithCriteria(filter, pageable);

        List<CryptoConvert> items = page.getContent().stream()
                .map(mapper::map)
                .toList();

        return CustomPage.of(items, page);

    }

    public CryptoConvert fallbackConvertAndPersist(ConvertRequest request, Throwable cause) {
        throw new ConversionFailedException("Upstream conversion unavailable", cause);
    }

}
