package com.github.milomarten.dice.term;

import java.math.BigDecimal;

/**
 * An implicit number term
 * This is the same as a NumberTerm, but marked in a type-safe way to indicate it was used implicitly
 * instead of explicitly. When implicit, the number should not be displayed in outputs, but still can be used
 * by evaluation to create the final output.
 * @param number The number to represent implicitly.
 */
public record ImplicitNumberTerm(BigDecimal number) implements DiceMathTerm {
    public static final ImplicitNumberTerm ONE = new ImplicitNumberTerm(BigDecimal.ONE);
    public static final ImplicitNumberTerm TWO = new ImplicitNumberTerm(BigDecimal.TWO);

    @Override
    public BigDecimal asNumber() {
        return number;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public String asString() {
        return number.toPlainString();
    }
}
