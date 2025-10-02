package com.casestudy.cryptoexchangeapi.logging.aop;

import com.casestudy.cryptoexchangeapi.common.exception.ApiException;
import com.casestudy.cryptoexchangeapi.logging.model.entity.LogEntity;
import com.casestudy.cryptoexchangeapi.logging.service.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Optional;

/**
 * Aspect for logging REST controller activity including successful responses and thrown exceptions.
 * Captures metadata such as request endpoint, method, user, status, and error messages,
 * and logs it into the database using {@link LogService}.
 * This aspect applies to all classes annotated with {@code @RestController}.
 *
 * @see LogEntity
 * @see LogService
 */
@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggerAspectJ {

    private final LogService logService;

    /**
     * Pointcut expression targeting all classes annotated with {@code @RestController}.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {

    }

    /**
     * Advice that logs exceptions thrown from any method within a {@code @RestController}.
     * Builds a {@link LogEntity} and stores it using {@link LogService}.
     *
     * @param joinPoint the join point representing the method that threw the exception
     * @param ex        the exception that was thrown
     */
    @AfterThrowing(pointcut = "restControllerPointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {

        Optional<ServletRequestAttributes> requestAttributes = Optional.ofNullable(
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        );

        if (requestAttributes.isPresent()) {

            final HttpServletRequest request = requestAttributes.get().getRequest();

            LogEntity logEntity = LogEntity.builder()
                    .endpoint(request.getRequestURL().toString())
                    .method(request.getMethod())
                    .message(ex.getMessage())
                    .errorType(ex.getClass().getName())
                    .status(String.valueOf(HttpStatus.valueOf(getHttpStatusFromException(ex))))
                    .operation(joinPoint.getSignature().getName())
                    .response(ex.getMessage())
                    .build();


            try {
                logService.saveLogToDatabase(logEntity);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.error("logAfterThrowing | Request Attributes are null!");
        }

    }

    /**
     * Advice that logs successful executions of controller methods.
     * Builds and stores a {@link LogEntity} after the method returns.
     *
     * @param joinPoint the join point representing the executed controller method
     * @param result    the result returned from the method
     * @throws IOException if JSON serialization fails
     */
    @AfterReturning(value = "restControllerPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) throws IOException {

        Optional<ServletRequestAttributes> requestAttributes = Optional.ofNullable(
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        );

        if (requestAttributes.isPresent()) {

            final HttpServletRequest request = requestAttributes.get().getRequest();
            final HttpServletResponse response = requestAttributes.get().getResponse();

            String responseObject = "";

            LogEntity logEntity = LogEntity.builder()
                    .endpoint(request.getRequestURL().toString())
                    .method(request.getMethod())
                    .operation(joinPoint.getSignature().getName())
                    .build();

            if (result instanceof JsonNode) {
                ObjectMapper objectMapper = new ObjectMapper();
                responseObject = objectMapper.writeValueAsString(result);
            } else {
                responseObject = result.toString();
            }

            logEntity.setResponse(responseObject);
            logEntity.setMessage(responseObject);
            Optional.ofNullable(response).ifPresent(
                    httpServletResponse -> logEntity.setStatus(
                            String.valueOf(HttpStatus.valueOf(httpServletResponse.getStatus()
                            ))
                    ));

            try {
                logService.saveLogToDatabase(logEntity);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        } else {
            log.error("logAfterReturning | Request Attributes are null!");
        }
    }

    /**
     * Resolves the appropriate {@link HttpStatus} for a given exception type.
     *
     * @param ex the thrown exception
     * @return the name of the corresponding HTTP status
     */
    private String getHttpStatusFromException(Exception ex) {
        // All your domain exceptions: EmailSendException, GithubLoginException,
        // ResourceNotFoundException, ScreenshotCaptureException, etc.
        if (ex instanceof ApiException apiEx) {
            return apiEx.getStatus().name(); // 503, 401, 404, 500 ... as defined per exception
        }

        // Validation errors
        if (ex instanceof MethodArgumentNotValidException || ex instanceof ConstraintViolationException) {
            return HttpStatus.BAD_REQUEST.name(); // 400
        }

        // Default: 500
        return HttpStatus.INTERNAL_SERVER_ERROR.name();

    }

}
