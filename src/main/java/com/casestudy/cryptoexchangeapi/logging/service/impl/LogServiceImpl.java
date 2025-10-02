package com.casestudy.cryptoexchangeapi.logging.service.impl;

import com.casestudy.cryptoexchangeapi.logging.model.entity.LogEntity;
import com.casestudy.cryptoexchangeapi.logging.repository.LogRepository;
import com.casestudy.cryptoexchangeapi.logging.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service implementation for handling log-related operations.
 * This interface defines a method for saving log entries to the database.
 * It abstracts the logic for logging and allows interaction with a persistence layer to store log data.
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;

    /**
     * Saves the provided {@link LogEntity} to the database.
     *
     * @param logEntity the {@link LogEntity} to be saved
     */
    @Override
    public void saveLogToDatabase(final LogEntity logEntity) {
        logEntity.setTime(LocalDateTime.now());
        logRepository.save(logEntity);
    }

}
