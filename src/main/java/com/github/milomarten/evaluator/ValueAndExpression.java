package com.github.milomarten.evaluator;

import java.util.List;

/**
 * Holds a term and the subparts that compose it.
 * @param value The term
 * @param operation The operation which created this ValueAndExpression
 * @param children The pieces of the operation that resulted in this ValueAndExpression
 * @param <T> The type of the term
 */
public record ValueAndExpression<T extends Term>(T value, Object operation, List<ValueAndExpression<T>> children) {
    public ValueAndExpression(T value) {
        this(value, null, List.of());
    }
}
