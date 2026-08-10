package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ValueAndExpression;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public record PredicateTerm(Comparison comparison, ValueAndExpression<DiceMathTerm> quantity) implements DiceMathTerm {
    @RequiredArgsConstructor
    public enum Comparison {
        LE('<'), GTE('>'), EQUAL('=');

        private final char symbol;

        public static Comparison read(char c) {
            return switch (c) {
                case '<' -> LE;
                case '>' -> GTE;
                case '=' -> EQUAL;
                default -> throw new IllegalArgumentException("" + c);
            };
        }
    }

    public PredicateTerm(Comparison comparison, DiceMathTerm quantity) {
        this(comparison, new ValueAndExpression<>(quantity));
    }

    public <T> Predicate<T> asObjIntPredicate(Function<T, Integer> mapper, EvaluatorOptions options) {
        return switch (this.comparison) {
            case LE -> t -> mapper.apply(t) <= quantity.value().asInteger(options);
            case GTE -> t -> mapper.apply(t) >= quantity.value().asInteger(options);
            case EQUAL -> t -> mapper.apply(t) == quantity.value().asInteger(options);
        };
    }

    public <T> Predicate<T> asObjBigDecimalPredicate(Function<T, BigDecimal> mapper) {
        return switch (this.comparison) {
            case LE -> t -> mapper.apply(t).compareTo(quantity.value().asNumber()) <= 0;
            case GTE -> t -> mapper.apply(t).compareTo(quantity.value().asNumber()) >= 0;
            case EQUAL -> t -> Objects.equals(mapper.apply(t), quantity.value().asNumber());
        };
    }

    public String asString() {
        return comparison.symbol + quantity.toString();
    }

    @Override
    public BigDecimal asNumber() {
        throw new UnsupportedOperationException("Tried to unwrap a predicate as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }
}
