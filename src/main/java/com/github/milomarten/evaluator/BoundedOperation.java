package com.github.milomarten.evaluator;

/**
 * Encapsulates an operation which straddles terms.
 * In addition to brackets, this also can handle the absolute value, or even
 * functions. Differing from a regular Operation, there is no priority (it is always treated
 * as highest priority)
 * @param <T> The type of the term
 */
public interface BoundedOperation<T extends Term> {
    /**
     * Get the left side of the bound
     * For example, (, [, or |
     * @return The left boundary string
     */
    String getLeftBound();

    /**
     * Get the right side of the bound
     * For example, ), ], or |
     * @return The right boundary string
     */
    String getRightBound();

    /**
     * Evaluate this operation and format it
     * @param termStack The term stack
     * @param options The options
     * @return A new value and its formatting
     */
    ValueAndExpression<T> evaluate(TermStack<T> termStack, EvaluatorOptions options);

    /**
     * Encompasses a standard bracketed expression.
     * The result of evaluating this expression is simply the top of the stack, essentially
     * doing nothing.
     * @param left The left bracket character
     * @param right The right bracket character
     * @param <T> The relevant terms
     */
    record Brackets<T extends Term>(char left, char right) implements BoundedOperation<T> {
        @Override
        public String getLeftBound() {
            return Character.toString(left);
        }

        @Override
        public String getRightBound() {
            return Character.toString(right);
        }

        @Override
        public ValueAndExpression<T> evaluate(TermStack<T> termStack, EvaluatorOptions options) {
            return termStack.pop();
        }
    }
}
