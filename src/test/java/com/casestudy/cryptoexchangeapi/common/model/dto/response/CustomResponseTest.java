package com.casestudy.cryptoexchangeapi.common.model.dto.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CustomResponseTest {

    @Test
    void successOf_sets_ok_status_success_true_and_payload() {
        // When
        LocalDateTime before = LocalDateTime.now();
        CustomResponse<String> res = CustomResponse.successOf("payload");
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(res.getIsSuccess()).isTrue();
        assertThat(res.getResponse()).isEqualTo("payload");
        assertThat(res.getTime()).isNotNull();
        // time is "now-ish"
        assertThat(!res.getTime().isBefore(before) && !res.getTime().isAfter(after)).isTrue();
    }

    @Test
    void createdOf_sets_created_status_success_true_and_payload() {
        // When
        LocalDateTime before = LocalDateTime.now();
        CustomResponse<Integer> res = CustomResponse.createdOf(42);
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getIsSuccess()).isTrue();
        assertThat(res.getResponse()).isEqualTo(42);
        assertThat(res.getTime()).isNotNull();
        assertThat(!res.getTime().isBefore(before) && !res.getTime().isAfter(after)).isTrue();
    }

    @Test
    void builder_allows_overriding_fields_manually() {
        LocalDateTime fixed = LocalDateTime.now().minusMinutes(5);

        CustomResponse<Double> res = CustomResponse.<Double>builder()
                .time(fixed)
                .httpStatus(HttpStatus.ACCEPTED)
                .isSuccess(false)
                .response(3.14)
                .build();

        assertThat(res.getTime()).isEqualTo(fixed);
        assertThat(res.getHttpStatus()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(res.getIsSuccess()).isFalse();
        assertThat(res.getResponse()).isEqualTo(3.14);
    }

}