package com.github.milomarten.dice.term;

import java.math.BigDecimal;

public record PlaceholderTerm() implements DiceMathTerm {
    public static final PlaceholderTerm INSTANCE = new PlaceholderTerm();

    @Override
    public BigDecimal asNumber() {
        throw new UnsupportedOperationException("Tried to unwrap a placeholder term as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }
}
