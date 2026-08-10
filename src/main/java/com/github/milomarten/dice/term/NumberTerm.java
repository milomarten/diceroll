package com.github.milomarten.dice.term;

import java.math.BigDecimal;

/**
 * A basic number
 * @param number The number, as a BigDecimal
 */
public record NumberTerm(BigDecimal number) implements DiceMathTerm {
    @Override
    public BigDecimal asNumber() {
        return number;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

}
