package com.github.milomarten.evaluator;

/**
 * Holds a term and the String representation of that term.
 * At the "leaf" of an expression, s is simply value.asString. However
 * this is not strictly enforced, and varies based on Operation configuration.
 * @param value The term
 * @param s The String representation
 * @param <T> The type of the term
 */
public record ValueAndExpression<T>(T value, String s) {
}
