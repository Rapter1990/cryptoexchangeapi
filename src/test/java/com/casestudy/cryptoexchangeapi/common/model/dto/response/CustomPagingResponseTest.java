package com.casestudy.cryptoexchangeapi.common.model.dto.response;

import com.casestudy.cryptoexchangeapi.common.model.CustomPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomPagingResponseTest {

    @Test
    void builder_of_sets_paging_fields_and_allows_content() {

        // Given: a CustomPage with known values
        CustomPage<String> page = new CustomPage<>() {
            @Override
            public Integer getPageNumber() { return 3; }

            @Override
            public Integer getPageSize() { return 25; }

            @Override
            public Long getTotalElementCount() { return 123L; }

            @Override
            public Integer getTotalPageCount() { return 5; }
        };

        // When: use builder.of(page) and then set content
        CustomPagingResponse<String> resp = CustomPagingResponse.<String>builder()
                .of(page)
                .content(List.of("a", "b", "c"))
                .build();

        // Then: metadata copied & content set
        assertThat(resp.getPageNumber()).isEqualTo(3);
        assertThat(resp.getPageSize()).isEqualTo(25);
        assertThat(resp.getTotalElementCount()).isEqualTo(123L);
        assertThat(resp.getTotalPageCount()).isEqualTo(5);
        assertThat(resp.getContent()).containsExactly("a", "b", "c");
    }

    @Test
    void builder_of_does_not_set_content_by_default() {

        CustomPage<Integer> page = new CustomPage<>() {
            @Override public Integer getPageNumber() { return 0; }
            @Override public Integer getPageSize() { return 10; }
            @Override public Long getTotalElementCount() { return 0L; }
            @Override public Integer getTotalPageCount() { return 0; }
        };

        CustomPagingResponse<Integer> resp = CustomPagingResponse.<Integer>builder()
                .of(page)
                .build();

        assertThat(resp.getContent()).isNull();

    }

}