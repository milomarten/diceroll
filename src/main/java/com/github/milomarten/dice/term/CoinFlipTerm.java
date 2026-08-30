package com.github.milomarten.dice.term;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

/**
 * All terms related to a Coin Flip
 * This exposes three new terms for dice operations:
 * - The letter C (COIN_FLIP)
 * - The letter H (HEADS)
 * - The letter T (TAILS)
 * Any of these terms can be used after a DICE operation to begin a coin flip, although the expected one is C. The output
 * of a coin flip will only ever be H or T. C, H, and T can also be used in pools directly, although this use may be limited.
 */
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

    @Override
    public String asString() {
        return letter;
    }


    public static Optional<CoinFlipTerm> parse(char c) {
        return Arrays.stream(values())
                .filter(cft -> cft.letter.charAt(0) == c)
                .findFirst();
    }
}
