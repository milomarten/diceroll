package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.math.BigDecimal;
import java.util.List;

public class PoolStrategy<T extends DiceMathTerm> implements TotalingStrategy<ValueAndExpression<T>> {
    @Override
    public BigDecimal totalUp(List<MarkedRoll<ValueAndExpression<T>>> markedRolls) {
        var validRolls = markedRolls
                .stream()
                .filter(mr -> !mr.dropped)
                .toList();
        if (validRolls.size() == 1) {
            return validRolls.getFirst().roll.value().asNumber();
        } else {
            throw new ExpressionSyntaxError("Tried to unwrap a pool as a number");
        }
    }

    @Override
    public boolean isNumber(List<MarkedRoll<ValueAndExpression<T>>> markedRolls) {
        return markedRolls.stream()
                .filter(mr -> !mr.dropped)
                .count() == 1;
    }
}
