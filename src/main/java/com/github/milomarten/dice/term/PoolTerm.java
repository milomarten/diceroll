package com.github.milomarten.dice.term;

import com.github.milomarten.dice.die.MarkedRoll;
import com.github.milomarten.evaluator.EvaluatorOptions;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public final class PoolTerm extends AbstractPoolTerm {
    public PoolTerm() {
        super();
    }

    public PoolTerm(DiceMathTerm one) {
        super(one);
    }

    public PoolTerm(DiceMathTerm one, DiceMathTerm two) {
        super(one, two);
    }

    public PoolTerm(List<MarkedRoll<DiceMathTerm>> pool) {
        super(pool);
    }

    private PoolTerm copyAndMutate(UnaryOperator<DiceMathTerm> func) {
        var newElements = getPool().stream()
                .map(mr -> new MarkedRoll<>(func.apply(mr.roll), mr.dropped, mr.exploded))
                .toList();
        return new PoolTerm(newElements);
    }

    @Override
    public DiceMathTerm add(DiceMathTerm addend, EvaluatorOptions options) {
        return copyAndMutate(i -> i.add(addend, options));
    }

    @Override
    public DiceMathTerm subtract(DiceMathTerm minuend, EvaluatorOptions options) {
        return copyAndMutate(i -> i.subtract(minuend, options));
    }

    @Override
    public DiceMathTerm multiply(DiceMathTerm multiplier, EvaluatorOptions options) {
        return copyAndMutate(i -> i.multiply(multiplier, options));
    }

    @Override
    public DiceMathTerm divide(DiceMathTerm divisor, EvaluatorOptions options) {
        return copyAndMutate(i -> i.divide(divisor, options));
    }

    @Override
    public DiceMathTerm root(DiceMathTerm radicand, EvaluatorOptions options) {
        return copyAndMutate(i -> i.root(radicand, options));
    }
}
