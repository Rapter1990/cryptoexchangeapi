package com.casestudy.cryptoexchangeapi.exchange.controller;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import com.casestudy.cryptoexchangeapi.common.model.dto.request.CustomPagingRequest;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomPagingResponse;
import com.casestudy.cryptoexchangeapi.common.model.dto.response.CustomResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.CryptoConvert;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.ConvertRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.request.FilterServicePagingRequest;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoConvertResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbol;
import com.casestudy.cryptoexchangeapi.exchange.model.dto.response.CryptoNameSymbolResponse;
import com.casestudy.cryptoexchangeapi.exchange.model.mapper.CryptoNameSymbolToCryptoNameSymbolResponseMapper;
import com.casestudy.cryptoexchangeapi.exchange.model.mapper.CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper;
import com.casestudy.cryptoexchangeapi.exchange.service.CryptoConvertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "01 - Crypto Convert API",
        description = "Convert between cryptocurrencies and query persisted conversion history. "
                + "POST /api/convert returns 201 with the saved conversion; "
                + "POST /api/convert/history supports filtering (from/to, amount & convertedAmount ranges, "
                + "createdAt range, transactionId substring) plus paging & sorting."
)
public class CryptoConvertController {

    private final CryptoConvertService service;

    private static final CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper PAGE_MAPPER =
            CustomPageCryptoConvertToCustomPagingCryptoConvertResponseMapper.initialize();

    @Operation(
            operationId = "convert",
            summary = "Convert an amount from one crypto to another and persist the result",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Conversion request",
                    content = @Content(
                            schema = @Schema(implementation = ConvertRequest.class),
                            examples = @ExampleObject(
                                    name = "BTC to ARB",
                                    value = """
                        {
                          "from": "BTC",
                          "to": "ARB",
                          "amount": 100
                        }
                        """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Conversion created",
                            content = @Content(
                                    schema = @Schema(implementation = CustomResponse.class),
                                    examples = @ExampleObject(
                                            name = "Created",
                                            value = """
                            {
                              "time": "2025-10-01T18:04:33.282",
                              "httpStatus": "CREATED",
                              "isSuccess": true,
                              "response": {
                                "createdAt": "2025-10-01T18:04:33.282",
                                "transactionId": "6c7de41f-71e5-4d63-984d-8dcb60ba6265",
                                "amount": 100,
                                "from": "BTC",
                                "to": "ARB",
                                "convertedAmount": 2711598539.488985400
                              }
                            }
                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "Upstream conversion unavailable (CMC error)",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<CryptoConvert> convert(@Valid @RequestBody ConvertRequest req) {

        CryptoConvert savedCryptoConvert = service.convertAndPersist(req);
        return CustomResponse.createdOf(savedCryptoConvert);

    }

    @Operation(
            operationId = "getHistory",
            summary = "Search conversion history with filters, pagination and sorting",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Filter, pagination and sorting",
                    content = @Content(
                            schema = @Schema(implementation = FilterServicePagingRequest.class),
                            examples = @ExampleObject(
                                    name = "Filter & page",
                                    value = """
                        {
                          "filterRequest": {
                            "filter": {
                              "from": "BTC",
                              "to": "ARB",
                              "minAmount": 50,
                              "maxAmount": 5000,
                              "minConvertedAmount": 1000000,
                              "maxConvertedAmount": 3000000000,
                              "createdAtFrom": "2025-09-29T00:00:00",
                              "createdAtTo": "2025-10-02T23:59:59",
                              "transactionIdContains": "6c7de4"
                            }
                          },
                          "pagingRequest": {
                            "pagination": { "pageNumber": 1, "pageSize": 20 },
                            "sorting": { "sortBy": "createdAt", "sortDirection": "DESC" }
                          }
                        }
                        """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paged result",
                            content = @Content(
                                    schema = @Schema(implementation = CustomPagingResponse.class),
                                    examples = @ExampleObject(
                                            name = "OK",
                                            value = """
                            {
                              "time": "2025-10-01T19:27:24.2492919",
                              "httpStatus": "OK",
                              "isSuccess": true,
                              "response": {
                                "content": [
                                  {
                                    "transactionId": "6c7de41f-71e5-4d63-984d-8dcb60ba6265",
                                    "amount": 100,
                                    "from": "BTC",
                                    "to": "ARB",
                                    "convertedAmount": 2711598539.488985400,
                                    "createdAt": "2025-10-01T18:04:33.282"
                                  }
                                ],
                                "pageNumber": 1,
                                "pageSize": 20,
                                "totalElementCount": 1,
                                "totalPageCount": 1
                              }
                            }
                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/history")
    public CustomResponse<CustomPagingResponse<CryptoConvertResponse>> getHistory(
            @Valid @RequestBody FilterServicePagingRequest filterServicePagingRequest) {

        CustomPage<CryptoConvert> page = service.getHistory(
                filterServicePagingRequest.getFilterRequest(),
                filterServicePagingRequest.getPagingRequest()
        );

        CustomPagingResponse<CryptoConvertResponse> response = PAGE_MAPPER.toPagingResponse(page);
        return CustomResponse.successOf(response);

    }

    @Operation(
            operationId = "cryptoMap",
            summary = "List cryptocurrencies (name + symbol) with pagination",
            description = "Returns a paged slice of cryptocurrencies from CoinMarketCap’s /v1/cryptocurrency/map.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "1-based page number",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "integer", defaultValue = "1", minimum = "1")
                    ),
                    @Parameter(
                            name = "size",
                            description = "page size",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "5000")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paged list of (name, symbol)",
                            content = @Content(
                                    schema = @Schema(implementation = CustomPagingResponse.class),
                                    examples = @ExampleObject(
                                            name = "OK",
                                            value = """
                                        {
                                          "time": "2025-10-01T19:27:24.2492919",
                                          "httpStatus": "OK",
                                          "isSuccess": true,
                                          "response": {
                                            "content": [
                                              { "name": "Bitcoin", "symbol": "BTC" },
                                              { "name": "Ethereum", "symbol": "ETH" }
                                            ],
                                            "pageNumber": 1,
                                            "pageSize": 20,
                                            "totalElementCount": 2,
                                            "totalPageCount": 2
                                          }
                                        }
                                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "CMC map call failed",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/map")
    public CustomResponse<CustomPagingResponse<CryptoNameSymbolResponse>> cryptoMap(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(5000) int size) {

        CustomPagingRequest pagingRequest = CustomPagingRequest.builder()
                .pagination(CustomPaging.builder()
                        .pageNumber(page)
                        .pageSize(size)
                        .build())
                .build();

        CustomPage<CryptoNameSymbol> pageResult = service.listCryptoNamesSymbols(pagingRequest);

        // Map domain -> response DTOs
        List<CryptoNameSymbolResponse> rows = pageResult.getContent().stream()
                .map(CryptoNameSymbolToCryptoNameSymbolResponseMapper.initialize()::map)
                .toList();

        CustomPagingResponse<CryptoNameSymbolResponse> payload = CustomPagingResponse.<CryptoNameSymbolResponse>builder()
                .content(rows)
                .pageNumber(pageResult.getPageNumber())
                .pageSize(pageResult.getPageSize())
                .totalElementCount(pageResult.getTotalElementCount())
                .totalPageCount(pageResult.getTotalPageCount())
                .build();

        return CustomResponse.successOf(payload);

    }

    @CacheEvict(allEntries = true, cacheNames = {"exchanges"})
    @PostConstruct
    @Scheduled(fixedRateString = "${cmc.cache-ttl}")
    public void clearCache() {
        log.info("Caches are cleared");
    }

}
