package com.casestudy.cryptoexchangeapi.exchange.repository;

import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.entity.CryptoConvertEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoConvertRepositoryCustomImpl implements CryptoConvertRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<CryptoConvertEntity> searchWithCriteria(ListCryptoConvertRequest.Filter filter, Pageable pageable) {
        Query query = new Query();

        // Build AND criteria in a list (easier to reason about & log)
        List<Criteria> ands = new ArrayList<>();

        if (filter != null) {
            // 1) Enums are stored as STRINGs in Mongo → compare by name()
            if (filter.getFrom() != null) {
                ands.add(Criteria.where("FROM_CURRENCY").is(filter.getFrom().name()));
            }
            if (filter.getTo() != null) {
                ands.add(Criteria.where("TO_CURRENCY").is(filter.getTo().name()));
            }

            // AMOUNT range (Decimal128)
            if (filter.getMinAmount() != null) {
                ands.add(Criteria.where("AMOUNT").gte(new Decimal128(filter.getMinAmount())));
            }
            if (filter.getMaxAmount() != null) {
                ands.add(Criteria.where("AMOUNT").lte(new Decimal128(filter.getMaxAmount())));
            }

            // CONVERTED_AMOUNT range (Decimal128)
            if (filter.getMinConvertedAmount() != null) {
                ands.add(Criteria.where("CONVERTED_AMOUNT").gte(new Decimal128(filter.getMinConvertedAmount())));
            }
            if (filter.getMaxConvertedAmount() != null) {
                ands.add(Criteria.where("CONVERTED_AMOUNT").lte(new Decimal128(filter.getMaxConvertedAmount())));
            }

            // 4) createdAt range — convert LocalDateTime -> Date in UTC to avoid tz drift
            if (filter.getCreatedAtFrom() != null) {
                Date fromUtc = Date.from(filter.getCreatedAtFrom().atZone(ZoneOffset.UTC).toInstant());
                ands.add(Criteria.where("createdAt").gte(fromUtc));
            }
            if (filter.getCreatedAtTo() != null) {
                Date toUtc = Date.from(filter.getCreatedAtTo().atZone(ZoneOffset.UTC).toInstant());
                ands.add(Criteria.where("createdAt").lte(toUtc));
            }

            // 5) TRANSACTION_ID contains (case-insensitive), safely quoted
            if (filter.getTransactionIdContains() != null && !filter.getTransactionIdContains().trim().isEmpty()) {
                String token = filter.getTransactionIdContains().trim();
                String regex = ".*" + java.util.regex.Pattern.quote(token) + ".*";
                ands.add(Criteria.where("TRANSACTION_ID").regex(regex, "i"));
            }
        }


        if (!ands.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(ands.toArray(new Criteria[0])));
        }

        // apply paging + sorting
        query.with(pageable);


        long total = mongoTemplate.count(query, CryptoConvertEntity.class);
        List<CryptoConvertEntity> entities = mongoTemplate.find(query, CryptoConvertEntity.class);

        return new PageImpl<>(entities, pageable, total);
    }

}
