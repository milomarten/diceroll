package com.github.milomarten.dice;

import com.github.milomarten.dice.operation.DiceOperation;
import com.github.milomarten.dice.term.*;
import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.Operation;
import com.github.milomarten.formatting.ExpressionFormatter;

import java.util.List;

public class DiceResultFormatter implements ExpressionFormatter<DiceMathTerm> {
    @Override
    public String formatTerm(DiceMathTerm value) {
        return switch (value) {
            case NumberTerm nt -> nt.number().toPlainString();
            case CoinFlipTerm ct -> ct.getLetter();
            case PlaceholderTerm ignored -> "";
            case ImplicitNumberTerm ignored -> "";
            case PredicateTerm pt -> pt.comparison().getSymbol() + formatTerm(pt.quantity());
            case PoolTerm pt -> pt.format(this);
        };
    }

    @Override
    public String formatOperation(Operation<DiceMathTerm> o) {
        if (o instanceof DiceOperation d) {
            return d.getSymbol();
        }
        return "#";
    }

    @Override
    public String formatBoundedOperation(BoundedOperation<DiceMathTerm> bo, List<String> contents) {
        return bo.getLeftBound() + String.join(", ", contents) + bo.getRightBound();
    }
}
