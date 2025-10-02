package com.casestudy.cryptoexchangeapi.exchange.repository;

import com.casestudy.cryptoexchangeapi.base.AbstractBaseServiceTest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CryptoConvertRepositoryCustomImplTest extends AbstractBaseServiceTest {

    @InjectMocks
    private CryptoConvertRepositoryCustomImpl cryptoConvertRepositoryCustomImpl;

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void searchWithCriteria_whenFilterNull_returnsEmptyPage_andInvokesMongo() {

        // Given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        // When
        when(mongoTemplate.count(any(Query.class), eq(CryptoConvertEntity.class)))
                .thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(CryptoConvertEntity.class)))
                .thenReturn(List.of());

        // Then
        Page<CryptoConvertEntity> page = cryptoConvertRepositoryCustomImpl.searchWithCriteria(null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(0L);
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getPageable()).isEqualTo(pageable);

        // Verify
        verify(mongoTemplate, times(1)).count(any(Query.class), eq(CryptoConvertEntity.class));
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(CryptoConvertEntity.class));
        verifyNoMoreInteractions(mongoTemplate);

    }

    @Test
    void searchWithCriteria_whenFilterProvided_buildsExpectedQuery_andReturnsPage() {

        // Given
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

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        CryptoConvertEntity e = CryptoConvertEntity.builder()
                .id("id-1")
                .transactionId("6c7de41f-71e5-4d63-984d-8dcb60ba6265")
                .build();

        // When
        when(mongoTemplate.count(any(Query.class), eq(CryptoConvertEntity.class)))
                .thenReturn(1L);

        when(mongoTemplate.find(any(Query.class), eq(CryptoConvertEntity.class)))
                .thenReturn(List.of(e));

        // Then
        Page<CryptoConvertEntity> page = cryptoConvertRepositoryCustomImpl.searchWithCriteria(f, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTransactionId())
                .isEqualTo("6c7de41f-71e5-4d63-984d-8dcb60ba6265");
        assertThat(page.getPageable()).isEqualTo(pageable);

        // Verify
        verify(mongoTemplate, times(1)).count(
                argThat((Query q) -> {
                    Document root = q.getQueryObject();

                    @SuppressWarnings("unchecked")
                    List<Document> ands = (List<Document>) root.get("$and");
                    assertThat(ands).isNotNull();

                    // helper to find the first sub-doc containing a key
                    java.util.function.Function<String, Document> getByKey = key ->
                            ands.stream().filter(d -> d.containsKey(key)).findFirst().orElse(null);

                    // FROM / TO
                    assertThat(getByKey.apply("FROM_CURRENCY")).isNotNull();
                    assertThat(getByKey.apply("FROM_CURRENCY").getString("FROM_CURRENCY")).isEqualTo("BTC");

                    assertThat(getByKey.apply("TO_CURRENCY")).isNotNull();
                    assertThat(getByKey.apply("TO_CURRENCY").getString("TO_CURRENCY")).isEqualTo("ARB");

                    // AMOUNT range (split across two AND docs)
                    Document amountGteDoc = ands.stream()
                            .filter(d -> d.containsKey("AMOUNT"))
                            .map(d -> (Document) d.get("AMOUNT"))
                            .filter(sub -> sub.containsKey("$gte"))
                            .findFirst().orElse(null);
                    Document amountLteDoc = ands.stream()
                            .filter(d -> d.containsKey("AMOUNT"))
                            .map(d -> (Document) d.get("AMOUNT"))
                            .filter(sub -> sub.containsKey("$lte"))
                            .findFirst().orElse(null);

                    assertThat(amountGteDoc).isNotNull();
                    assertThat(amountGteDoc.get("$gte")).isInstanceOf(Decimal128.class);
                    assertThat(((Decimal128) amountGteDoc.get("$gte")).bigDecimalValue())
                            .isEqualByComparingTo("50");

                    assertThat(amountLteDoc).isNotNull();
                    assertThat(amountLteDoc.get("$lte")).isInstanceOf(Decimal128.class);
                    assertThat(((Decimal128) amountLteDoc.get("$lte")).bigDecimalValue())
                            .isEqualByComparingTo("5000");

                    // CONVERTED_AMOUNT range
                    Document convGteDoc = ands.stream()
                            .filter(d -> d.containsKey("CONVERTED_AMOUNT"))
                            .map(d -> (Document) d.get("CONVERTED_AMOUNT"))
                            .filter(sub -> sub.containsKey("$gte"))
                            .findFirst().orElse(null);
                    Document convLteDoc = ands.stream()
                            .filter(d -> d.containsKey("CONVERTED_AMOUNT"))
                            .map(d -> (Document) d.get("CONVERTED_AMOUNT"))
                            .filter(sub -> sub.containsKey("$lte"))
                            .findFirst().orElse(null);

                    assertThat(convGteDoc).isNotNull();
                    assertThat(convGteDoc.get("$gte")).isInstanceOf(Decimal128.class);
                    assertThat(((Decimal128) convGteDoc.get("$gte")).bigDecimalValue())
                            .isEqualByComparingTo("1000000");

                    assertThat(convLteDoc).isNotNull();
                    assertThat(convLteDoc.get("$lte")).isInstanceOf(Decimal128.class);
                    assertThat(((Decimal128) convLteDoc.get("$lte")).bigDecimalValue())
                            .isEqualByComparingTo("3000000000");

                    // createdAt range
                    Date fromUtc = Date.from(f.getCreatedAtFrom().atZone(ZoneOffset.UTC).toInstant());
                    Date toUtc   = Date.from(f.getCreatedAtTo().atZone(ZoneOffset.UTC).toInstant());

                    Document createdGteDoc = ands.stream()
                            .filter(d -> d.containsKey("createdAt"))
                            .map(d -> (Document) d.get("createdAt"))
                            .filter(sub -> sub.containsKey("$gte"))
                            .findFirst().orElse(null);
                    Document createdLteDoc = ands.stream()
                            .filter(d -> d.containsKey("createdAt"))
                            .map(d -> (Document) d.get("createdAt"))
                            .filter(sub -> sub.containsKey("$lte"))
                            .findFirst().orElse(null);

                    assertThat(createdGteDoc).isNotNull();
                    assertThat(createdGteDoc.get("$gte")).isEqualTo(fromUtc);

                    assertThat(createdLteDoc).isNotNull();
                    assertThat(createdLteDoc.get("$lte")).isEqualTo(toUtc);

                    // TRANSACTION_ID regex — accept Pattern **or** Document
                    Document txDoc = getByKey.apply("TRANSACTION_ID");
                    assertThat(txDoc).isNotNull();
                    Object txCond = txDoc.get("TRANSACTION_ID");

                    if (txCond instanceof java.util.regex.Pattern p) {
                        // pattern should be .*\\Q6c7de4\\E.* and case-insensitive
                        assertThat(p.pattern()).contains("6c7de4");
                        assertThat((p.flags() & java.util.regex.Pattern.CASE_INSENSITIVE) != 0).isTrue();
                    } else if (txCond instanceof Document txCondDoc) {
                        boolean hasOptions =
                                (txCondDoc.containsKey("$options") && "i".equals(txCondDoc.getString("$options"))) ||
                                        (txCondDoc.containsKey("$regularExpression")
                                                && txCondDoc.get("$regularExpression") instanceof Document re
                                                && "i".equals(re.getString("options")));
                        assertThat(hasOptions).isTrue();
                    } else {
                        fail("Unexpected TRANSACTION_ID condition type: " + txCond);
                    }

                    // pageable applied
                    assertThat(q.getSortObject()).isNotNull();
                    assertThat(q.getSortObject().toJson()).contains("createdAt");
                    return true;
                }),
                eq(CryptoConvertEntity.class)
        );

        verify(mongoTemplate, times(1)).find(
                argThat((Query q) -> {
                    Document root = q.getQueryObject();

                    @SuppressWarnings("unchecked")
                    List<Document> ands = (List<Document>) root.get("$and");
                    assertThat(ands).isNotNull();

                    // helper to find the first sub-doc containing a key
                    Function<String, Document> getByKey = key ->
                            ands.stream().filter(d -> d.containsKey(key)).findFirst().orElse(null);

                    // FROM / TO are inside $and
                    Document fromDoc = getByKey.apply("FROM_CURRENCY");
                    Document toDoc   = getByKey.apply("TO_CURRENCY");

                    assertThat(fromDoc).isNotNull();
                    assertThat(toDoc).isNotNull();
                    assertThat(fromDoc.getString("FROM_CURRENCY")).isEqualTo("BTC");
                    assertThat(toDoc.getString("TO_CURRENCY")).isEqualTo("ARB");
                    assertThat(q.getSortObject()).isNotNull();
                    assertThat(q.getSortObject().toJson()).contains("createdAt");

                    return true;
                }),
                eq(CryptoConvertEntity.class)
        );

        verifyNoMoreInteractions(mongoTemplate);

    }

}