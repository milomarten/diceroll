package com.github.milomarten.evaluator;

import lombok.RequiredArgsConstructor;

import java.util.Deque;
import java.util.LinkedList;

/**
 * A wrapper around a Deque of Terms.
 * In addition to the terms themselves, this class allows us to also keep track of our "work", so to speak.
 * This work is a running string representation of the evaluation, for debugging or display to a user. As an example:
 * 1. A new empty stack is created
 * 2. The term 10 is pushed to the stack, with the string "10" is also included
 * 3. The term 5 is pushed to the stack, with the string "5" is also included
 * 4. The multiplication operator is invoked. The stack is popped twice, and pushes the term 50 to the stack. Depending
 * on the operator configuration, it attaches the string "(10 * 5)" with it.
 * 5. The term 7 is pushed to the stack, with the string "7".
 * 6. The addition operator is invoked, the stack is popped twice, and pushes the term 57 to the stack. Depending on
 * the operator configuration, it attaches the string "((10 * 5) + 7) with it.
 * 7. Evaluation is complete. The answer is 57, and the order of operations was "((10 * 5) + 7)".
 * @param <T> The subclass of Term provided
 */
@RequiredArgsConstructor
public class TermStack<T extends Term> {
    private final Deque<ValueAndExpression<T>> stack = new LinkedList<>();

    private final EvaluatorOptions options;

    private int termCounter = 0;

    /**
     * Push a term to the stack.
     * If a maximum number of terms is enforced, and this term exceeds that limit, an exception
     * is thrown.
     * @param term The term to add
     */
    public void push(T term) {
        if (options.hasTermMaximum()) {
            termCounter++;
            if (termCounter > options.getMaximumNumberOfTerms()) {
                throw new ExpressionSyntaxError("Too many terms! Encountered " + termCounter);
            }
        }

        pushInternal(new ValueAndExpression<>(term));
    }

    void pushInternal(ValueAndExpression<T> expression) {
        this.stack.push(expression);
    }

    /**
     * Get the size of the stack
     * @return The stack size
     */
    public int size() {
        return stack.size();
    }

    /**
     *
     * @return
     */
    public ValueAndExpression<T> pop() {
        return stack.pop();
    }
}
