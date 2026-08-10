package com.github.milomarten.evaluator;

import java.util.List;

/**
 * Holds a term and the String representation of that term.
 * At the "leaf" of an expression, s is simply value.asString. However
 * this is not strictly enforced, and varies based on Operation configuration.
 * @param value The term
 * @param <T> The type of the term
 */
public record ValueAndExpression<T extends Term>(T value, Object operation, List<ValueAndExpression<T>> children) {
    public ValueAndExpression(T value) {
        this(value, null, List.of());
    }
}
