package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.TokenTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class TokenDie implements Die<DiceMathTerm> {
    private final TokenTerm token;

    @Override
    public List<DiceMathTerm> roll(int qty, EvaluatorOptions options) {
        return IntStream.range(0, qty)
                .mapToObj(t -> token.wrapped().get())
                .map(c -> (DiceMathTerm) c)
                .toList();
    }

    @Override
    public DiceMathTerm getMaxValue(EvaluatorOptions options) {
        throw new ExpressionSyntaxError("Max value unattainable");
    }
}
