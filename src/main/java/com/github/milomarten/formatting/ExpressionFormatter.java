package com.github.milomarten.formatting;

import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.Operation;
import com.github.milomarten.evaluator.Term;

import java.util.List;

/**
 * A formatter designed for expressions
 * @param <T> The term type
 */
public interface ExpressionFormatter<T extends Term> {
    /**
     * Format a term into a string
     * @param value The value to format
     * @return The result
     */
    String formatTerm(T value);

    /**
     * Format an operation into a string
     * @param o The operation to format
     * @param operatedTerms The terms under operation
     * @return The result
     */
    String formatOperation(Operation<T> o, List<String> operatedTerms);

    /**
     * Format a bounded operation into a string
     * @param bo The bounded operation
     * @param contents The terms within the bounds, already formatted via formatTerm
     * @return The result
     */
    String formatBoundedOperation(BoundedOperation<T> bo, List<String> contents);
}
