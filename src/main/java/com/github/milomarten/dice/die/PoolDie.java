package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.PoolTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PoolDie implements Die<ValueAndExpression<DiceMathTerm>> {
    private static final Random RANDOM = new Random();

    private final PoolTerm poolTerm;

    public PoolDie(PoolTerm poolTerm) {
        this.poolTerm = poolTerm;
    }

    @Override
    public List<ValueAndExpression<DiceMathTerm>> roll(int qty, EvaluatorOptions options) {
        if (qty < 0) {
            throw new ExpressionSyntaxError("Num times rolling dice < 0");
        }

        var possibilities = poolTerm.getPool()
                .stream()
                .filter(mr -> !mr.dropped)
                .toList();
        if (possibilities.isEmpty()) {
            throw new ExpressionSyntaxError("No possible values in the pool");
        }

        return IntStream.range(0, qty)
                .map(i -> RANDOM.nextInt(0, possibilities.size()))
                .mapToObj(i -> poolTerm.getPool().get(i).roll)
                .toList();
    }

    @Override
    public ValueAndExpression<DiceMathTerm> getMaxValue(EvaluatorOptions options) {
        return poolTerm.getPool()
                .stream()
                .filter(mr -> !mr.dropped)
                .max(PoolTerm.LOWEST_FIRST)
                .map(mr -> mr.roll)
                .orElseThrow(() -> new ExpressionSyntaxError("No possible values in the pool"));
    }
}
