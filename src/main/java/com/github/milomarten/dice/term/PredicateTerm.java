package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.ExpressionSyntaxError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A term which indicates a numerical predicate.
 * A predicate is a union of a comparison operator, and a term to use. This term cannot be unwrapped
 * as a number.
 * It is an error to use Comparsion.LE or Comparison.GTE with a quantity that is non-numeric.Comparison.EQUAL
 * can be used with any term
 * @param comparison The comparison to make
 * @param quantity The term to compare against
 */
public record PredicateTerm(Comparison comparison, DiceMathTerm quantity) implements DiceMathTerm {
    @RequiredArgsConstructor
    public enum Comparison {
        LTE("<"), GTE(">"), EQUAL("=");

        @Getter private final String symbol;
    }

    public <T> Predicate<T> asObjBigDecimalPredicate(Function<T, BigDecimal> mapper) {
        return switch (this.comparison) {
            case LTE -> t -> mapper.apply(t).compareTo(quantity.asNumber()) <= 0;
            case GTE -> t -> mapper.apply(t).compareTo(quantity.asNumber()) >= 0;
            case EQUAL -> t -> Objects.equals(t, quantity);
        };
    }

    @Override
    public BigDecimal asNumber() {
        throw new ExpressionSyntaxError("Tried to unwrap a predicate as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public String asString() {
        throw new ExpressionSyntaxError("Tried to unwrap a predicate as a string");
    }
}
