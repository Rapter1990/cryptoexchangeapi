package com.casestudy.cryptoexchangeapi.exchange.model.dto.request.validator;

import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CryptoPairValidatorTest {

    private CryptoPairValidator validator;

    // Mocks for building constraint violations
    private ConstraintValidatorContext ctx;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeCtx;

    @BeforeEach
    void setUp() {
        validator = new CryptoPairValidator();

        ctx = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        nodeCtx = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        // Return the same chain for all violation creations
        when(ctx.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeCtx);
        when(nodeCtx.addConstraintViolation()).thenReturn(ctx);
    }

    // ---------- happy paths ----------

    @Test
    void isValid_whenEnumsProvidedAndValid_returnsTrue_andNoViolations() {
        BeanEnum bean = new BeanEnum(EnumCryptoCurrency.BTC, EnumCryptoCurrency.ETH);

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isTrue();
        verify(ctx, never()).buildConstraintViolationWithTemplate(anyString());
        verify(ctx, never()).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_whenStringsProvidedAndValid_ignoresCase_returnsTrue() {
        BeanString bean = new BeanString("btc", "ArB");

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isTrue();
        verify(ctx, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_whenValueIsNull_returnsTrue() {
        assertThat(validator.isValid(null, ctx)).isTrue();
        verifyNoInteractions(ctx);
    }

    // ---------- invalid inputs -> violations ----------

    @Test
    void isValid_whenFromStringInvalid_addsViolationOnFrom_andReturnsFalse() {
        BeanString bean = new BeanString("NOT_A_COIN", "ETH");

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isFalse();

        // default violation disabled once
        verify(ctx, times(1)).disableDefaultConstraintViolation();
        // exact message + field node
        verify(ctx, times(1))
                .buildConstraintViolationWithTemplate("'from' must be a valid crypto symbol (e.g., BTC, ETH)");
        verify(builder, times(1)).addPropertyNode("from");
        verify(nodeCtx, times(1)).addConstraintViolation();

        // no 'to' violation in this case
        verify(builder, never()).addPropertyNode("to");
    }

    @Test
    void isValid_whenToStringInvalid_addsViolationOnTo_andReturnsFalse() {
        BeanString bean = new BeanString("ETH", "___");

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isFalse();

        verify(ctx, times(1)).disableDefaultConstraintViolation();
        verify(ctx, times(1))
                .buildConstraintViolationWithTemplate("'to' must be a valid crypto symbol (e.g., BTC, ETH)");
        verify(builder, times(1)).addPropertyNode("to");
        verify(nodeCtx, times(1)).addConstraintViolation();

        verify(builder, never()).addPropertyNode("from");
    }

    @Test
    void isValid_whenBothInvalid_addsTwoViolations_andReturnsFalse() {
        BeanString bean = new BeanString("bad", "worse");

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isFalse();

        // called twice (once per violation)
        verify(ctx, times(2)).disableDefaultConstraintViolation();

        // once for 'from', once for 'to'
        verify(ctx, times(1))
                .buildConstraintViolationWithTemplate("'from' must be a valid crypto symbol (e.g., BTC, ETH)");
        verify(ctx, times(1))
                .buildConstraintViolationWithTemplate("'to' must be a valid crypto symbol (e.g., BTC, ETH)");

        verify(builder, times(1)).addPropertyNode("from");
        verify(builder, times(1)).addPropertyNode("to");
        verify(nodeCtx, times(2)).addConstraintViolation();
    }

    @Test
    void isValid_whenUnsupportedTypeProvided_addsViolationOnFrom_andReturnsFalse() {
        BeanMixedTypes bean = new BeanMixedTypes(123, EnumCryptoCurrency.ETH); // unsupported type for 'from'

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isFalse();
        verify(ctx, times(1)).disableDefaultConstraintViolation();
        verify(ctx, times(1))
                .buildConstraintViolationWithTemplate("'from' must be a valid crypto symbol (e.g., BTC, ETH)");
        verify(builder, times(1)).addPropertyNode("from");
        verify(nodeCtx, times(1)).addConstraintViolation();
        // 'to' valid → no second violation
        verify(builder, never()).addPropertyNode("to");
    }

    @Test
    void isValid_whenNoGetters_privateFieldsOnly_validatorReadsFieldsDirectly() {
        BeanNoGetter bean = new BeanNoGetter("BTC", "ETH"); // private fields, no getters

        boolean ok = validator.isValid(bean, ctx);

        assertThat(ok).isTrue();
        verifyNoInteractions(ctx);
    }

    // ---------- helper beans ----------

    private static class BeanEnum {
        private final EnumCryptoCurrency from;
        private final EnumCryptoCurrency to;

        BeanEnum(EnumCryptoCurrency from, EnumCryptoCurrency to) {
            this.from = from;
            this.to = to;
        }
        public EnumCryptoCurrency getFrom() { return from; }
        public EnumCryptoCurrency getTo()   { return to; }
    }

    private static class BeanString {
        private final String from;
        private final String to;

        BeanString(String from, String to) {
            this.from = from;
            this.to = to;
        }
        public String getFrom() { return from; }
        public String getTo()   { return to; }
    }

    private static class BeanMixedTypes {
        private final Object from;                  // intentionally Object
        private final EnumCryptoCurrency to;

        BeanMixedTypes(Object from, EnumCryptoCurrency to) {
            this.from = from;
            this.to = to;
        }
        public Object getFrom() { return from; }
        public EnumCryptoCurrency getTo() { return to; }
    }

    private static class BeanNoGetter {
        // private fields, no getters – exercises validator's field-access fallback
        private final String from;
        private final String to;

        private BeanNoGetter(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }
}
