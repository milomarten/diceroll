package com.github.milomarten.dice.die;


import com.github.milomarten.evaluator.Term;
import com.github.milomarten.formatting.ExpressionFormatter;

import java.math.BigDecimal;
import java.util.List;

/**
 * For dice rolls, describe how the results of a roll should be totaled up
 * @param <T> The type of term
 */
public interface TotalingStrategy<T extends Term> {
    /**
     * Sum up the list of rolls into one number
     * If this strategy doesn't support returning a number, throw an ExpressionSyntaxError
     * @param rolls The rolls to sum up
     * @return A BigDecimal representing the total
     */
    BigDecimal totalUp(List<MarkedRoll<T>> rolls);

    /**
     * Check if this strategy can total the rolls into a number
     * @param rolls The rolls to sum up
     * @return True if the rolls can be represented as a number
     */
    boolean isNumber(List<MarkedRoll<T>> rolls);

    /**
     * Format the list of rolls into a string
     * All TotalingStrategies must at least output a summary of each roll.
     * @param formatter The formatter to use
     * @param rolls The rolls to summarize
     * @return A string representation of the rolls
     */
    String formatSummary(ExpressionFormatter<T> formatter, List<MarkedRoll<T>> rolls);
}
