package com.casestudy.cryptoexchangeapi.logging.aop;

import ch.qos.logback.classic.Level;
import com.casestudy.cryptoexchangeapi.base.AbstractBaseServiceTest;
import com.casestudy.cryptoexchangeapi.exchange.exception.ConversionFailedException;
import com.casestudy.cryptoexchangeapi.logging.model.entity.LogEntity;
import com.casestudy.cryptoexchangeapi.logging.service.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggerAspectJTest extends AbstractBaseServiceTest {

    @InjectMocks
    private LoggerAspectJ loggerAspectJ;

    @Mock
    private LogService logService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and set request attributes
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(servletRequestAttributes.getResponse()).thenReturn(httpServletResponse);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        // Mock JoinPoint signature
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(signature.getDeclaringTypeName()).thenReturn("LoggerAspectJ");
        when(signature.getDeclaringType()).thenReturn(LoggerAspectJ.class);
    }

    @Test
    void logAfterThrowing_withConversionFailedException_persistsBadGateway() {

        ConversionFailedException ex = new ConversionFailedException("Upstream conversion unavailable");

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/convert"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(signature.getName()).thenReturn("convert");

        loggerAspectJ.logAfterThrowing(joinPoint, ex);

        verify(logService).saveLogToDatabase(argThat(log ->
                "http://localhost/api/convert".equals(log.getEndpoint()) &&
                        "POST".equals(log.getMethod()) &&
                        "convert".equals(log.getOperation()) &&
                        ex.getMessage().equals(log.getMessage()) &&
                        ex.getMessage().equals(log.getResponse()) &&
                        ConversionFailedException.class.getName().equals(log.getErrorType()) &&
                        String.valueOf(HttpStatus.BAD_GATEWAY).equals(log.getStatus())
        ));

    }

    @Test
    void logAfterThrowing_whenSaveLogThrows_logsErrorButDoesNotPropagate() {

        ConversionFailedException ex = new ConversionFailedException("Upstream conversion unavailable");

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/convert"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(signature.getName()).thenReturn("convert");

        doThrow(new RuntimeException("Database error")).when(logService).saveLogToDatabase(any());

        assertDoesNotThrow(() -> loggerAspectJ.logAfterThrowing(joinPoint, ex));

        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));

        Optional<String> databaseError = logTracker.checkMessage(Level.ERROR, "Database error");
        assertTrue(databaseError.isPresent(), "Expected ERROR log with 'Database error' message to be present");

    }

    @Test
    public void testLogAfterReturning() throws IOException {

        // When
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletResponse.getStatus()).thenReturn(HttpStatus.OK.value());
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);

        // Then
        loggerAspectJ.logAfterReturning(joinPoint, "test response");

        // Verify
        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));

    }

    @Test
    public void testLogAfterReturning_WithJsonNode() throws IOException {

        // Given
        JsonNode jsonNode = new ObjectMapper().createObjectNode().put("key", "value");

        // When
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletResponse.getStatus()).thenReturn(HttpStatus.OK.value());

        // Then
        loggerAspectJ.logAfterReturning(joinPoint, jsonNode);

        // Verify
        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));

    }

    @Test
    public void testLogAfterReturning_NoResponseAttributes() throws IOException {

        // Given
        RequestContextHolder.resetRequestAttributes();

        // When
        loggerAspectJ.logAfterReturning(mock(JoinPoint.class), "test response");

        // Then
        verify(logService, never()).saveLogToDatabase(any(LogEntity.class));

    }

    @Test
    public void testLogAfterReturning_SaveLogThrowsException() throws IOException {

        // When
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletResponse.getStatus()).thenReturn(HttpStatus.OK.value());
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        doThrow(new RuntimeException("Database error")).when(logService).saveLogToDatabase(any(LogEntity.class));

        // Then
        assertDoesNotThrow(() -> loggerAspectJ.logAfterReturning(joinPoint, "test response"));

        // Verify
        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));

    }

    @Test
    void testLogAfterThrowing_whenRequestAttributesAreNull_thenLogError() {

        // Given
        RequestContextHolder.resetRequestAttributes();
        Exception ex = new RuntimeException("Some error");

        // When
        loggerAspectJ.logAfterThrowing(joinPoint, ex);

        // Then
        Optional<String> logMessage = logTracker.checkMessage(Level.ERROR, "logAfterThrowing | Request Attributes are null!");
        assertTrue(logMessage.isPresent(), "Expected error log message not found.");
        assertEquals(logMessage.get(), "logAfterThrowing | Request Attributes are null!");

    }

    @Test
    void testGetHttpStatusFromException_AllCases() {
        // Given
        BindingResult dummyBinding = mock(BindingResult.class);

        // Cases according to LoggerAspectJ#getHttpStatusFromException:
        // - ApiException -> ex.getStatus().name()
        // - MethodArgumentNotValidException, ConstraintViolationException -> BAD_REQUEST
        // - Default -> INTERNAL_SERVER_ERROR
        var cases = Map.ofEntries(
                Map.entry(new MethodArgumentNotValidException(null, dummyBinding),
                        HttpStatus.BAD_REQUEST.name()),
                Map.entry(new ConstraintViolationException(Collections.emptySet()),
                        HttpStatus.BAD_REQUEST.name()),
                Map.entry(new ConversionFailedException("any"),
                        new ConversionFailedException("x").getStatus().name()),
                Map.entry(new RuntimeException("oops"),
                        HttpStatus.INTERNAL_SERVER_ERROR.name()),
                Map.entry(new Exception("unknown"),
                        HttpStatus.INTERNAL_SERVER_ERROR.name())
        );

        // When / Then
        cases.forEach((exc, expected) -> {
            String actual = (String) org.springframework.test.util.ReflectionTestUtils
                    .invokeMethod(loggerAspectJ, "getHttpStatusFromException", exc);
            assertEquals(expected, actual, "Mismatch for: " + exc.getClass().getSimpleName());
        });

    }

}