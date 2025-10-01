package com.casestudy.cryptoexchangeapi.exchange.model.dto.request.validator;

import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CryptoPairValidator implements ConstraintValidator<CryptoPairValid, Object> {

    private static final String FROM_FIELD = "from";
    private static final String TO_FIELD   = "to";

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true; // let @NotNull handle nulls on fields

        Object rawFrom = readProperty(value, FROM_FIELD);
        Object rawTo   = readProperty(value, TO_FIELD);

        boolean ok = true;

        // Validate 'from' membership if present
        if (rawFrom != null && resolveEnum(rawFrom) == null) {
            addViolation(context, FROM_FIELD,
                    "'from' must be a valid crypto symbol (e.g., BTC, ETH)");
            ok = false;
        }

        // Validate 'to' membership if present
        if (rawTo != null && resolveEnum(rawTo) == null) {
            addViolation(context, TO_FIELD,
                    "'to' must be a valid crypto symbol (e.g., BTC, ETH)");
            ok = false;
        }

        return ok;
    }

    private Object readProperty(Object bean, String name) {
        try {
            String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method m = bean.getClass().getMethod(getter);
            return m.invoke(bean);
        } catch (Exception ignore) {
            try {
                Field f = bean.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return f.get(bean);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private EnumCryptoCurrency resolveEnum(Object raw) {
        if (raw instanceof EnumCryptoCurrency) return (EnumCryptoCurrency) raw;
        if (raw instanceof String) {
            String s = ((String) raw).trim();
            if (s.isEmpty()) return null;
            try {
                return EnumCryptoCurrency.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return null; // unsupported type -> invalid
    }

    private void addViolation(ConstraintValidatorContext ctx, String field, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

}
