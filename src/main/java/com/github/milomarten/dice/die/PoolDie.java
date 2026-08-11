package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.PoolTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * A dice which has a Pool of possibly non-numeric options
 * Each option in the pool is equally likely to be returned. Within the pool itself, any possible term
 * type is supported.
 * getMaxValue will throw an exception only if any non-dropped term is the pool is non-numeric.
 */
@RequiredArgsConstructor
public class PoolDie implements Die<DiceMathTerm> {
    private final PoolTerm poolTerm;
    private final UniformRandomProvider random;

    @Override
    public List<DiceMathTerm> roll(int qty, EvaluatorOptions options) {
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
                .map(i -> random.nextInt(0, possibilities.size()))
                .mapToObj(i -> poolTerm.getPool().get(i).roll)
                .toList();
    }

    @Override
    public DiceMathTerm getMaxValue(EvaluatorOptions options) {
        return poolTerm.getPool()
                .stream()
                .filter(mr -> !mr.dropped)
                .max(PoolTerm.LOWEST_FIRST)
                .map(mr -> mr.roll)
                .orElseThrow(() -> new ExpressionSyntaxError("No possible values in the pool"));
    }
}
