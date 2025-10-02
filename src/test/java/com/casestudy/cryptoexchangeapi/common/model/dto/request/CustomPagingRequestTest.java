package com.casestudy.cryptoexchangeapi.common.model.dto.request;

import com.casestudy.cryptoexchangeapi.common.model.CustomSorting;
import jakarta.validation.ConstraintViolation;
import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomPagingRequestTest {

    @Test
    void toPageable_withSorting_returnsPageableWithSort() {

        // Given
        CustomPaging pagination = mock(CustomPaging.class);
        when(pagination.getPageNumber()).thenReturn(3);
        when(pagination.getPageSize()).thenReturn(50);

        Sort expectedSort = Sort.by(Sort.Order.desc("updatedAt"))
                .and(Sort.by(Sort.Order.asc("id")));
        CustomSorting sorting = mock(CustomSorting.class);
        when(sorting.toSort()).thenReturn(expectedSort);

        CustomPagingRequest req = CustomPagingRequest.builder()
                .pagination(pagination)
                .sorting(sorting)
                .build();

        // When
        Pageable pageable = req.toPageable();

        // Then
        assertThat(pageable).isInstanceOf(PageRequest.class);
        assertThat(pageable.getPageNumber()).isEqualTo(3);
        assertThat(pageable.getPageSize()).isEqualTo(50);

        // Sort equivalence
        assertThat(pageable.getSort()).isEqualTo(expectedSort);
        assertThat(pageable.getSort().isSorted()).isTrue();

        verify(pagination, atLeastOnce()).getPageNumber();
        verify(pagination, atLeastOnce()).getPageSize();
        verify(sorting, atLeastOnce()).toSort();
        verifyNoMoreInteractions(pagination, sorting);
    }

    @Test
    void toPageable_withoutSorting_returnsUnsorted() {
        // Given
        CustomPaging pagination = mock(CustomPaging.class);
        when(pagination.getPageNumber()).thenReturn(1);
        when(pagination.getPageSize()).thenReturn(20);

        CustomPagingRequest req = CustomPagingRequest.builder()
                .pagination(pagination)
                .sorting(null) // explicitly absent
                .build();

        // When
        Pageable pageable = req.toPageable();

        // Then
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().isUnsorted()).isTrue();

        verify(pagination, atLeastOnce()).getPageNumber();
        verify(pagination, atLeastOnce()).getPageSize();
        verifyNoMoreInteractions(pagination);
    }

    @Test
    void validation_fails_whenPaginationIsNull() {
        // Given
        CustomPagingRequest req = CustomPagingRequest.builder()
                .pagination(null)
                .sorting(null)
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        // When
        Set<ConstraintViolation<CustomPagingRequest>> violations = validator.validate(req);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anySatisfy(v -> {
            // field path is correct
            assertThat(v.getPropertyPath().toString()).isEqualTo("pagination");
            // locale-independent check: template for @NotNull
            assertThat(v.getMessageTemplate())
                    .isEqualTo("{jakarta.validation.constraints.NotNull.message}");
            // message itself may be localized, but should not be blank
            assertThat(v.getMessage()).isNotBlank();
        });
    }

}