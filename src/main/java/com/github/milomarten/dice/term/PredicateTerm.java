package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ValueAndExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public record PredicateTerm(Comparison comparison, DiceMathTerm quantity) implements DiceMathTerm {
    @RequiredArgsConstructor
    public enum Comparison {
        LE("<"), GTE(">"), EQUAL("=");

        @Getter private final String symbol;

        public static Comparison read(char c) {
            return switch (c) {
                case '<' -> LE;
                case '>' -> GTE;
                case '=' -> EQUAL;
                default -> throw new IllegalArgumentException("" + c);
            };
        }
    }

    public <T> Predicate<T> asObjBigDecimalPredicate(Function<T, BigDecimal> mapper) {
        return switch (this.comparison) {
            case LE -> t -> mapper.apply(t).compareTo(quantity.asNumber()) <= 0;
            case GTE -> t -> mapper.apply(t).compareTo(quantity.asNumber()) >= 0;
            case EQUAL -> t -> Objects.equals(mapper.apply(t), quantity.asNumber());
        };
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
