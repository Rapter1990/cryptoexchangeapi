package com.casestudy.cryptoexchangeapi.common.model.dto.response;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Generic response structure for paginated data.
 *
 * @param <T> the type of content returned in the page
 */
@Getter
@Builder
public class CustomPagingResponse<T> {

    private List<T> content;

    private Integer pageNumber;

    private Integer pageSize;

    private Long totalElementCount;

    private Integer totalPageCount;

    /**
     * Builder class for {@link CustomPagingResponse}.
     *
     * @param <T> the type of the response content
     */
    public static class CustomPagingResponseBuilder<T> {

        /**
         * Initializes the builder fields from a {@link CustomPage} instance.
         *
         * @param customPage the source page metadata
         * @param <C>        type of original content (not used in this method)
         * @return builder with metadata fields set
         */
        public <C> CustomPagingResponseBuilder<T> of(final CustomPage<C> customPage) {
            return CustomPagingResponse.<T>builder()
                    .pageNumber(customPage.getPageNumber())
                    .pageSize(customPage.getPageSize())
                    .totalElementCount(customPage.getTotalElementCount())
                    .totalPageCount(customPage.getTotalPageCount());
        }

    }

}
