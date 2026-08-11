package com.github.milomarten.dice.operation;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.PoolTerm;
import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.TermStack;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.util.List;

public enum PoolOperation implements BoundedOperation<DiceMathTerm> {
    INSTANCE;

    @Override
    public String getLeftBound() {
        return "{";
    }

    @Override
    public String getRightBound() {
        return "}";
    }

    @Override
    public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
        var contents = termStack.pop();
        if (contents.value() instanceof PoolTerm) {
            return contents;
        } else {
            var pool = new PoolTerm(contents.value());
            return new ValueAndExpression<>(pool, this, List.of(contents));
        }
    }
}
