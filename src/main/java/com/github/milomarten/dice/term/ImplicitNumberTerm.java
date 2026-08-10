package com.github.milomarten.dice.term;

import java.math.BigDecimal;

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

}
