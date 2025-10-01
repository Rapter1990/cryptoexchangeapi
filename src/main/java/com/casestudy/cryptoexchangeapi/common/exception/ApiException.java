package com.casestudy.cryptoexchangeapi.common.exception;

import com.casestudy.cryptoexchangeapi.common.model.CustomError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }

    /** Per-exception HTTP status (mirrors your STATUS constant). */
    public abstract HttpStatus getStatus();

    /** Which CustomError.Header should be used for this exception. */
    public abstract CustomError.Header getHeader();

}
