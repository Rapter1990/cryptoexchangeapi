package com.casestudy.cryptoexchangeapi.exchange.model.dto.request.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CryptoPairValidator.class)
public @interface CryptoPairValid {
    String message() default "Invalid crypto symbol";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
