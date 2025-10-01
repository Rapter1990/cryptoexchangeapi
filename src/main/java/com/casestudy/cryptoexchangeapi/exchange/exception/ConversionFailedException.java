package com.casestudy.cryptoexchangeapi.exchange.exception;
 // adjust package if needed
import com.casestudy.cryptoexchangeapi.common.exception.ApiException;
import com.casestudy.cryptoexchangeapi.common.model.CustomError;
import org.springframework.http.HttpStatus;

public class ConversionFailedException extends ApiException {

    public static final HttpStatus STATUS = HttpStatus.BAD_GATEWAY;
    public static final CustomError.Header HEADER = CustomError.Header.PROCESS_ERROR;

    public ConversionFailedException(String reason) {
        super("Conversion failed: " + reason);
    }

    public ConversionFailedException(String reason, Throwable cause) {
        super("Conversion failed: " + reason);
        initCause(cause);
    }

    @Override
    public HttpStatus getStatus() {
        return STATUS;
    }

    @Override
    public CustomError.Header getHeader() {
        return HEADER;
    }

}
