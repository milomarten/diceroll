package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class DiscreteDie implements Die<ValueAndExpression<DiceMathTerm>> {
    private static final Random RANDOM = new Random();

    private final char letter;
    private final List<ValueAndExpression<DiceMathTerm>> possibilities;

    public DiscreteDie(char letter, DiceMathTerm... options) {
        this.letter = letter;
        this.possibilities = Arrays.stream(options)
                .map(ValueAndExpression::new)
                .toList();
    }

    @Override
    public List<ValueAndExpression<DiceMathTerm>> roll(int qty, EvaluatorOptions options) {
        return IntStream.range(0, qty)
                .map(i -> RANDOM.nextInt(0, possibilities.size()))
                .mapToObj(possibilities::get)
                .toList();
    }

    @Override
    public ValueAndExpression<DiceMathTerm> getMaxValue(EvaluatorOptions options) {
        throw new ExpressionSyntaxError("No maximum value possible for a coin flip or fate die");
    }
}
