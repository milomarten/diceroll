package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.ExpressionSyntaxError;

import java.math.BigDecimal;

/**
 * A generic term which represents nothing
 * This term cannot be unwrapped to a number, and requires explicit handling on a per-operation basis.
 * A PlaceholderTerm should be created only by operators which can handle them.
 * One example is the explode operator, which permits the right term to be an implicit "whatever the maximum
 * dice roll" is. This PlaceholderTerm is used ONLY whenever the right term, and the explode operator has explicit
 * logic to fill in the placeholder during evaluation with the true value
 */
public record PlaceholderTerm() implements DiceMathTerm {
    public static final PlaceholderTerm INSTANCE = new PlaceholderTerm();

    @Override
    public BigDecimal asNumber() {
        throw new ExpressionSyntaxError("Tried to unwrap a placeholder term as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public String asString() {
        throw new ExpressionSyntaxError("Tried to unwrap a placeholder term as a string");
    }
}
