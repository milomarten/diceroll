package com.github.milomarten.dice.term;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public enum CoinFlipTerm implements DiceMathTerm {
    COIN_FLIP("C"), HEADS("H"), TAILS("T");

    @Getter private final String letter;

    @Override
    public BigDecimal asNumber() {
        throw new UnsupportedOperationException("Tried to unwrap " + name() + " as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    public static Optional<CoinFlipTerm> parse(char c) {
        return Arrays.stream(values())
                .filter(cft -> cft.letter.charAt(0) == c)
                .findFirst();
    }
}
