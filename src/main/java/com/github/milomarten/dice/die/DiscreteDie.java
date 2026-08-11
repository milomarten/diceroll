package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class DiscreteDie implements Die<DiceMathTerm> {
    private static final Random RANDOM = new Random();

    private final char letter;
    private final List<DiceMathTerm> possibilities;

    public DiscreteDie(char letter, DiceMathTerm... options) {
        this.letter = letter;
        this.possibilities = Arrays.asList(options);
    }

    @Override
    public List<DiceMathTerm> roll(int qty, EvaluatorOptions options) {
        return IntStream.range(0, qty)
                .map(i -> RANDOM.nextInt(0, possibilities.size()))
                .mapToObj(possibilities::get)
                .toList();
    }

    @Override
    public DiceMathTerm getMaxValue(EvaluatorOptions options) {
        throw new ExpressionSyntaxError("No maximum value possible for a coin flip or fate die");
    }
}
