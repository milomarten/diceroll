package com.github.milomarten.formatting;

import com.github.milomarten.dice.operation.DiceOperation;
import com.github.milomarten.dice.term.*;
import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.Operation;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.util.ArrayList;
import java.util.List;

public class LineByLineFormatter {
    public static List<String> format(ValueAndExpression<DiceMathTerm> tree) {
        var output = new ArrayList<String>();

        var node = copy(0, null, tree);
        do {
            node.format(new F());
            output.add(node.getString());
            node.pullUp();
        } while (!node.isLeaf());
        node.format(new F());
        output.add(node.getString());

        return output;
    }

    private static UnNode<DiceMathTerm> copy(int ctr, UnNode<DiceMathTerm> parent, ValueAndExpression<DiceMathTerm> tree) {
        var unnode = new UnNode<>(ctr, parent, tree);
        for (var child : tree.children()) {
            unnode.getChildren().add(copy(ctr + 1, unnode, child));
        }
        return unnode;
    }

    private static class F implements ExpressionFormatter<DiceMathTerm> {
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
}
