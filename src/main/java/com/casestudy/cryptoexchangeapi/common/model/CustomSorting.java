package com.casestudy.cryptoexchangeapi.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

/**
 * Represents optional sorting configuration for pageable data.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CustomSorting {

    private String sortBy;         // Optional: e.g., "createdAt", "eventId", etc.
    private String sortDirection; // Optional: "ASC" or "DESC"

    /**
     * Converts this object into a Spring Data {@link Sort} object.
     *
     * @return a {@link Sort} object representing the sort configuration, or {@link Sort#unsorted()} if sortBy is empty
     */
    public Sort toSort() {
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            return Sort.by(direction, sortBy);
        }
        return Sort.unsorted();
    }

}
