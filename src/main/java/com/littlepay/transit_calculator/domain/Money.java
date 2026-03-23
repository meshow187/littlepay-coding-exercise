package com.littlepay.transit_calculator.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public record Money(BigDecimal amount) {
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public static Money of(double value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    // Formats the output for the CSV (e.g., "$3.25")
    public String format() {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(amount);
    }
}
