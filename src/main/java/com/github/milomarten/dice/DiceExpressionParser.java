package com.github.milomarten.dice;

import com.github.milomarten.dice.operation.DiceOperation;
import com.github.milomarten.dice.operation.PoolOperation;
import com.github.milomarten.dice.term.CoinFlipTerm;
import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.NumberTerm;
import com.github.milomarten.dice.term.PredicateTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionEvaluator;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.parsing.BasicExpressionParser;
import com.github.milomarten.parsing.ShrinkingString;

public class DiceExpressionParser extends BasicExpressionParser<DiceMathTerm> {
    @Override
    protected DiceMathTerm stringToTerm(ShrinkingString string, EvaluatorOptions options) {
        var c = string.currentChar();
        var coinFlipMaybe = CoinFlipTerm.parse(c);
        if (coinFlipMaybe.isPresent()) {
            string.advance();
            return coinFlipMaybe.get();
        }

        return BasicExpressionParser.readNextBigDecimal(string)
                .map(NumberTerm::new)
                .orElse(null);
    }

    @Override
    protected void handleNonTerm(ShrinkingString string, EvaluatorOptions options, ExpressionEvaluator<DiceMathTerm> evaluator) {
        var c = string.currentChar();
        if (c == '=' || c == '<' || c == '>') {
            var comp = PredicateTerm.Comparison.read(c);
            string.advance();
            var number = BasicExpressionParser.readNextBigDecimal(string);

            evaluator.push(new PredicateTerm(comp, new NumberTerm(number.orElseThrow(() -> new ExpressionSyntaxError("Predicate without number after it")))));
        } else if (c == '{') {
            evaluator.pushBoundedOperationStart(PoolOperation.INSTANCE);
            string.advance();
        } else if (c == '}') {
            evaluator.pushBoundedOperationEnd("}");
            string.advance();
        } else {
            var operation = DiceOperation.findBestPossibleMatch(string.toString());
            if (operation.isPresent()) {
                string.advance(operation.get().getSymbol().length());
                evaluator.push(operation.get());
            } else {
                super.handleNonTerm(string, options, evaluator);
            }
        }
    }
}
