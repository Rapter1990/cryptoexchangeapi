package com.casestudy.cryptoexchangeapi.exchange.config;

import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ListCryptoConvertRequest;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

@Configuration
public class HistoryKeyConfig {

    @Bean("historyKeyGenerator")
    public KeyGenerator historyKeyGenerator() {
        return (target, method, params) -> {
            ListCryptoConvertRequest req = (ListCryptoConvertRequest) params[0];

            // Accept either Pageable or CustomPagingRequest in param[1]
            Pageable pageable = null;
            if (params.length > 1 && params[1] != null) {
                Object p1 = params[1];
                if (p1 instanceof Pageable p) {
                    pageable = p;
                } else if (p1 instanceof CustomPagingRequest cpr) {
                    pageable = cpr.toPageable(); // your own converter
                }
            }
            if (pageable == null) {
                pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            }

            var f = (req == null) ? null : req.getFilter();

            StringBuilder sb = new StringBuilder("history::");
            if (f != null) {
                sb.append("from=").append(Objects.toString(f.getFrom(), ""))
                        .append("|to=").append(Objects.toString(f.getTo(), ""))
                        .append("|minAmt=").append(Objects.toString(f.getMinAmount(), ""))
                        .append("|maxAmt=").append(Objects.toString(f.getMaxAmount(), ""))
                        .append("|minConv=").append(Objects.toString(f.getMinConvertedAmount(), ""))
                        .append("|maxConv=").append(Objects.toString(f.getMaxConvertedAmount(), ""))
                        .append("|fromDate=").append(Objects.toString(f.getCreatedAtFrom(), ""))
                        .append("|toDate=").append(Objects.toString(f.getCreatedAtTo(), ""))
                        .append("|txPart=").append(Objects.toString(f.getTransactionIdContains(), ""));
            } else {
                sb.append("nofilter");
            }

            sb.append("|page=").append(pageable.getPageNumber())
                    .append("|size=").append(pageable.getPageSize())
                    .append("|sort=");

            pageable.getSort().forEach(o ->
                    sb.append(o.getProperty()).append(":").append(o.getDirection()).append(",")
            );

            return sb.toString();
        };

    }

}

