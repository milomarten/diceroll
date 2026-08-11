package com.github.milomarten.dice.die;

import com.github.milomarten.evaluator.EvaluatorOptions;

import java.util.List;

/**
 * Represents an abstract object that can randomly provide discrete values
 * @param <T> The output of the dice
 */
public interface Die<T> {
    /**
     * Roll some number of dice and compile the results
     * @param qty The quantity of dice
     * @param options The evaluator options
     * @return A list of the dice rolls
     */
    List<T> roll(int qty, EvaluatorOptions options);

    /**
     * Get the maximum value of this dice, if applicable
     * For non-numerical dice, this may throw an ExpressionSyntaxError
     * @param options The evaluator options
     * @return The maximum value.
     */
    T getMaxValue(EvaluatorOptions options);
}
