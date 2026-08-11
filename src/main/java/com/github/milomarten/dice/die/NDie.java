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

@RequiredArgsConstructor
public class NDie implements Die<DiceMathTerm> {
    private static final Random RANDOM = new Random();

    private final DiceMathTerm numFaces;

    @Override
    public List<DiceMathTerm> roll(int numTimes, EvaluatorOptions options) {
        if (numTimes < 0) {
            throw new ExpressionSyntaxError("Num times rolling dice < 0");
        }
        var numFacesInt = getMaxValue(options).asInteger(options);
        if (numFacesInt < 1) {
            throw new ExpressionSyntaxError("Num faces on dice < 1");
        }
        return IntStream.range(0, numTimes)
                .map(i -> 1 + RANDOM.nextInt(0, numFacesInt))
                .boxed()
                .map(this::fromInt)
                .toList();
    }

    @Override
    public DiceMathTerm getMaxValue(EvaluatorOptions options) {
        return fromInt(numFaces.asInteger(options));
    }

    private DiceMathTerm fromInt(int i) {
        return new NumberTerm(BigDecimal.valueOf(i));
    }
}
