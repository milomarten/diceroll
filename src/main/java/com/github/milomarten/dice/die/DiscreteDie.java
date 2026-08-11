package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * A dice which has a discrete amount of non-numeric options
 * This dice is most useful for coin flips or fudge dice. Each possibility
 * is equally likely.
 * getMaxValue will always throw an exception, since the expected use case for this
 * die is for non-numeric options.
 */
public class DiscreteDie implements Die<DiceMathTerm> {
    private final List<DiceMathTerm> possibilities;
    private final UniformRandomProvider random;

    public DiscreteDie(UniformRandomProvider random, DiceMathTerm... options) {
        if (options.length == 0) {
            throw new ExpressionSyntaxError("No options passed to DiscreteDie");
        }
        this.random = random;
        this.possibilities = Arrays.asList(options);
    }

    @Override
    public List<DiceMathTerm> roll(int qty, EvaluatorOptions options) {
        return IntStream.range(0, qty)
                .map(i -> random.nextInt(0, possibilities.size()))
                .mapToObj(possibilities::get)
                .toList();
    }

    @Override
    public DiceMathTerm getMaxValue(EvaluatorOptions options) {
        throw new ExpressionSyntaxError("No maximum value possible for a coin flip or fate die");
    }
}
