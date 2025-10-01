package com.casestudy.cryptoexchangeapi.exchange.model.entity;

import com.casestudy.cryptoexchangeapi.common.model.entity.BaseEntity;
import com.casestudy.cryptoexchangeapi.exchange.model.enums.EnumCryptoCurrency;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crypto-convert-collection")
public class CryptoConvertEntity extends BaseEntity {

    @Id
    @Indexed(unique = true)
    private String id;

    @Field("TRANSACTION_ID")
    private String transactionId;

    @Field(name = "AMOUNT", targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    @Field(name = "FROM_CURRENCY", targetType = FieldType.STRING)
    private EnumCryptoCurrency fromCurrency;

    @Field(name = "TO_CURRENCY", targetType = FieldType.STRING)
    private EnumCryptoCurrency toCurrency;

    @Field(name = "CONVERTED_AMOUNT", targetType = FieldType.DECIMAL128)
    private BigDecimal convertedAmount;

}