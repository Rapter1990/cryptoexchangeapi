package com.casestudy.cryptoexchangeapi.exchange.model.mapper;

import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoConvertResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CryptoConvertToCryptoConvertResponseMapperTest {

    private final CryptoConvertToCryptoConvertResponseMapper mapper =
            Mappers.getMapper(CryptoConvertToCryptoConvertResponseMapper.class);

    // ---- Null & simple list behaviors ----

    @Test
    void testMapDomainNull() {
        CryptoConvertResponse result = mapper.map((CryptoConvert) null);
        assertNull(result);
    }

    @Test
    void testMapResponseNull() {
        CryptoConvert result = mapper.map((CryptoConvertResponse) null);
        assertNull(result);
    }

    @Test
    void testMapResponseListNull() {
        List<CryptoConvert> result = mapper.map((List<CryptoConvertResponse>) null);
        assertNull(result);
    }

    @Test
    void testMapResponseListEmpty() {
        List<CryptoConvert> result = mapper.map(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMapResponseListWithNullElements() {
        CryptoConvertResponse r1 = CryptoConvertResponse.builder()
                .transactionId("tx-1")
                .amount(new BigDecimal("123.45"))
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ARB)
                .convertedAmount(new BigDecimal("6789.01"))
                .createdAt(LocalDateTime.now())
                .build();

        CryptoConvertResponse r2 = null;

        List<CryptoConvertResponse> responses = Arrays.asList(r1, r2);
        List<CryptoConvert> result = mapper.map(responses);

        assertNotNull(result);
        // MapStruct generates a list with the same size, mapping each entry
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNull(result.get(1));
    }

    @Test
    void testMapDomainToResponseWithValidValues() {
        LocalDateTime now = LocalDateTime.now();

        CryptoConvert domain = CryptoConvert.builder()
                .transactionId("6c7de41f-71e5-4d63-984d-8dcb60ba6265")
                .amount(new BigDecimal("100.00000000"))
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ARB)
                .convertedAmount(new BigDecimal("2711598539.488985400"))
                .createdAt(now)
                .build();

        CryptoConvertResponse out = mapper.map(domain);

        assertNotNull(out);
        assertEquals(domain.getTransactionId(), out.getTransactionId());
        assertEquals(domain.getAmount(), out.getAmount());
        assertEquals(domain.getFrom(), out.getFrom());
        assertEquals(domain.getTo(), out.getTo());
        assertEquals(domain.getConvertedAmount(), out.getConvertedAmount());
        assertEquals(domain.getCreatedAt(), out.getCreatedAt());
    }

    @Test
    void testMapResponseToDomainWithValidValues() {
        LocalDateTime now = LocalDateTime.now();

        CryptoConvertResponse response = CryptoConvertResponse.builder()
                .transactionId("abc-123")
                .amount(new BigDecimal("2.5"))
                .from(EnumCryptoCurrency.ETH)
                .to(EnumCryptoCurrency.BTC)
                .convertedAmount(new BigDecimal("0.1"))
                .createdAt(now)
                .build();

        CryptoConvert domain = mapper.map(response);

        assertNotNull(domain);
        assertEquals(response.getTransactionId(), domain.getTransactionId());
        assertEquals(response.getAmount(), domain.getAmount());
        assertEquals(response.getFrom(), domain.getFrom());
        assertEquals(response.getTo(), domain.getTo());
        assertEquals(response.getConvertedAmount(), domain.getConvertedAmount());
        assertEquals(response.getCreatedAt(), domain.getCreatedAt());
    }

    @Test
    void testMapResponseListWithValidValues() {
        CryptoConvertResponse r1 = CryptoConvertResponse.builder()
                .transactionId("tx-1")
                .amount(new BigDecimal("1"))
                .from(EnumCryptoCurrency.ARB)
                .to(EnumCryptoCurrency.BTC)
                .convertedAmount(new BigDecimal("0.00001"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        CryptoConvertResponse r2 = CryptoConvertResponse.builder()
                .transactionId("tx-2")
                .amount(new BigDecimal("3.14"))
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.ETH)
                .convertedAmount(new BigDecimal("42.0"))
                .createdAt(LocalDateTime.now())
                .build();

        List<CryptoConvert> out = mapper.map(Arrays.asList(r1, r2));

        assertNotNull(out);
        assertEquals(2, out.size());

        assertEquals(r1.getTransactionId(), out.get(0).getTransactionId());
        assertEquals(r1.getFrom(), out.get(0).getFrom());
        assertEquals(r1.getTo(), out.get(0).getTo());
        assertEquals(r1.getAmount(), out.get(0).getAmount());
        assertEquals(r1.getConvertedAmount(), out.get(0).getConvertedAmount());
        assertEquals(r1.getCreatedAt(), out.get(0).getCreatedAt());

        assertEquals(r2.getTransactionId(), out.get(1).getTransactionId());
        assertEquals(r2.getFrom(), out.get(1).getFrom());
        assertEquals(r2.getTo(), out.get(1).getTo());
        assertEquals(r2.getAmount(), out.get(1).getAmount());
        assertEquals(r2.getConvertedAmount(), out.get(1).getConvertedAmount());
        assertEquals(r2.getCreatedAt(), out.get(1).getCreatedAt());
    }

    @Test
    void testMapDomainToResponseWithEdgeCaseValues() {

        CryptoConvert domain = CryptoConvert.builder()
                .transactionId("")
                .amount(BigDecimal.ZERO)
                .from(EnumCryptoCurrency.BTC)
                .to(EnumCryptoCurrency.BTC)
                .convertedAmount(BigDecimal.ZERO)
                .createdAt(null)
                .build();

        CryptoConvertResponse out = mapper.map(domain);

        assertNotNull(out);
        assertEquals("", out.getTransactionId());
        assertEquals(BigDecimal.ZERO, out.getAmount());
        assertEquals(EnumCryptoCurrency.BTC, out.getFrom());
        assertEquals(EnumCryptoCurrency.BTC, out.getTo());
        assertEquals(BigDecimal.ZERO, out.getConvertedAmount());
        assertNull(out.getCreatedAt());
    }

}
