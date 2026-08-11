package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.NumberTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * A dice which outputs 1 to n as an option
 * Represents your usual polyhedral dice, any value from 1 to numFaces has equal likelihood
 * of returning.
 */
public class NDie implements Die<DiceMathTerm> {
    private static final Random RANDOM = new Random();

    private final int numFaces;

    /**
     * Create a die with some number of faces
     * Having a non-positive number of faces is an error.
     * @param numFaces The number of faces
     */
    public NDie(int numFaces) {
        if (numFaces < 1) {
            throw new ExpressionSyntaxError("Num faces on dice < 1");
        }
        this.numFaces = numFaces;
    }

    @Override
    public List<DiceMathTerm> roll(int numTimes, EvaluatorOptions options) {
        if (numTimes < 0) {
            throw new ExpressionSyntaxError("Num times rolling dice < 0");
        }
        return IntStream.range(0, numTimes)
                .map(i -> 1 + RANDOM.nextInt(0, numFaces))
                .boxed()
                .map(this::fromInt)
                .toList();
    }

    @Override
    public DiceMathTerm getMaxValue(EvaluatorOptions options) {
        return fromInt(numFaces);
    }

    private DiceMathTerm fromInt(int i) {
        return new NumberTerm(BigDecimal.valueOf(i));
    }
}
