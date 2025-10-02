package com.casestudy.cryptoexchangeapi.common.exception;

import com.casestudy.cryptoexchangeapi.base.AbstractBaseServiceTest;
import com.casestudy.cryptoexchangeapi.common.model.CustomError;
import com.casestudy.cryptoexchangeapi.exchange.exception.ConversionFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest extends AbstractBaseServiceTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleMethodArgumentNotValid_returnsBadRequest_withSubErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        FieldError fe = new FieldError("obj", "age", "must be >= 18");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fe));

        ResponseEntity<Object> resp = globalExceptionHandler.handleMethodArgumentNotValid(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Validation failed")
                .subErrors(List.of(CustomError.CustomSubError.builder()
                        .field("age")
                        .message("must be >= 18")
                        .build()))
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());

    }

    @Test
    void handleConstraintViolation_returnsBadRequest_withMappedViolations() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> cv = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);

        when(cv.getMessage()).thenReturn("must be positive");
        when(cv.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("dto.amount");
        when(cv.getInvalidValue()).thenReturn(-5);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));

        ResponseEntity<Object> resp = globalExceptionHandler.handlePathVariableErrors(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Constraint violation")
                .subErrors(List.of(CustomError.CustomSubError.builder()
                        .message("must be positive")
                        .field("amount")
                        .value("-5")
                        .type("Integer")
                        .build()))
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());

    }

    @Test
    void handleRuntimeException_returnsBadRequestWithMessage() {

        // Given
        RuntimeException ex = new RuntimeException("unexpected boom");

        // When
        ResponseEntity<?> resp = globalExceptionHandler.handleRuntimeException(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.API_ERROR.getName())
                .message("unexpected boom")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());

    }

    @Test
    void handleConversionFailed_returnsBadGateway_processErrorHeader() {

        // Given
        ConversionFailedException ex = new ConversionFailedException("Upstream conversion unavailable");

        // When
        ResponseEntity<Object> resp = globalExceptionHandler.handleConversionFailed(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);

        CustomError expected = CustomError.builder()
                .httpStatus(HttpStatus.BAD_GATEWAY)
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .message("Conversion failed: Upstream conversion unavailable")
                .build();

        checkCustomError(expected, (CustomError) resp.getBody());
    }

    private void checkCustomError(CustomError expectedError, CustomError actualError) {

        assertThat(actualError).isNotNull();
        assertThat(actualError.getTime()).isNotNull();
        assertThat(actualError.getHeader()).isEqualTo(expectedError.getHeader());
        assertThat(actualError.getIsSuccess()).isEqualTo(expectedError.getIsSuccess());

        if (expectedError.getMessage() != null) {
            assertThat(actualError.getMessage()).isEqualTo(expectedError.getMessage());
        }

        if (expectedError.getSubErrors() != null) {
            assertThat(actualError.getSubErrors().size()).isEqualTo(expectedError.getSubErrors().size());
            if (!expectedError.getSubErrors().isEmpty()) {
                assertThat(actualError.getSubErrors().getFirst().getMessage()).isEqualTo(expectedError.getSubErrors().get(0).getMessage());
                assertThat(actualError.getSubErrors().getFirst().getField()).isEqualTo(expectedError.getSubErrors().get(0).getField());
                assertThat(actualError.getSubErrors().getFirst().getValue()).isEqualTo(expectedError.getSubErrors().get(0).getValue());
                assertThat(actualError.getSubErrors().getFirst().getType()).isEqualTo(expectedError.getSubErrors().get(0).getType());
            }
        }

    }

}