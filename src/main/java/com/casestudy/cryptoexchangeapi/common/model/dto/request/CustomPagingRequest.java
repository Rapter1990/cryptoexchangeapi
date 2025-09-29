package com.casestudy.cryptoexchangeapi.common.model.dto.request;

import com.casestudy.cryptoexchangeapi.common.model.CustomPaging;
import com.casestudy.cryptoexchangeapi.common.model.CustomSorting;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Abstract class that represents a custom paging request.
 * This class provides pagination details through a `CustomPaging` object
 * and includes a method to convert the pagination information into a `Pageable` object.
 * It is intended to be extended by other classes that require paging functionality.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomPagingRequest {

    @NotNull
    private CustomPaging pagination;

    private CustomSorting sorting; // Optional

    /**
     * Converts the request into a Spring Data {@link Pageable} object
     * with optional sorting.
     *
     * @return Pageable instance based on pagination and sorting settings
     */
    public Pageable toPageable() {
        Sort sort = (sorting != null) ? sorting.toSort() : Sort.unsorted();

        return PageRequest.of(
                Math.toIntExact(pagination.getPageNumber()),
                Math.toIntExact(pagination.getPageSize()),
                sort
        );

    }

}
