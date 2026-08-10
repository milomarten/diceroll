package com.github.milomarten.evaluator;

/**
 * Indicates an error parsing a dice expression
 */
public class ExpressionSyntaxError extends RuntimeException {
    public ExpressionSyntaxError(String message) {
        super(message);
    }
}
