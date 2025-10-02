package com.casestudy.cryptoexchangeapi.logging.service.impl;

import com.casestudy.cryptoexchangeapi.base.AbstractBaseServiceTest;
import com.casestudy.cryptoexchangeapi.logging.model.entity.LogEntity;
import com.casestudy.cryptoexchangeapi.logging.repository.LogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogServiceImplTest extends AbstractBaseServiceTest {

    @InjectMocks
    private LogServiceImpl logService;

    @Mock
    private LogRepository logRepository;

    @Test
    void saveLogToDatabase_setsTimeAndPersists() {

        // Given
        LogEntity input = LogEntity.builder()
                .endpoint("http://localhost/api/foo")
                .method("GET")
                .message("ok")
                .status("200 OK")
                .build();

        // When
        when(logRepository.save(any(LogEntity.class))).thenReturn(input);

        // Then
        logService.saveLogToDatabase(input);

        // Verify
        verify(logRepository, times(1)).save(any(LogEntity.class));

    }

}