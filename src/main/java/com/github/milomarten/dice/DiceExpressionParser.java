package com.github.milomarten.dice;

import com.github.milomarten.dice.operation.DiceOperation;
import com.github.milomarten.dice.operation.PoolOperation;
import com.github.milomarten.dice.term.*;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionEvaluator;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.parsing.BasicExpressionParser;
import com.github.milomarten.parsing.ShrinkingString;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A parser for parsing diceroll strings
 * Terms supported:
 * - any decimal number,
 * - the letters C, H, and T (which represent coin flips)
 * - The characters =, &lt;, and &gt;, followed by a number, which represents a predicate
 * - Any letters between double- or single-quotes, representing a generic string. Backslash is used as an escape character
 * - An @ followed by any Latin letter (upper- or lowercase or an underscore), representing a Token
 * Operations supported:
 * - Bounded Operator &#123;&#125;, for constructing Pools
 * - All Operators found in DiceOperation
 */
public class DiceExpressionParser extends BasicExpressionParser<DiceMathTerm> {
    public static final Set<Integer> TOKEN_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_"
            .chars()
            .boxed()
            .collect(Collectors.toSet());

    @Override
    protected DiceMathTerm stringToTerm(ShrinkingString string, EvaluatorOptions options) {
        var c = string.currentChar();

        if (c == '@') {
            var tokenMaybe = BasicExpressionParser.readToken(string, TOKEN_CHARS, '@');
            if (tokenMaybe.isPresent()) {
                var resolve = options.getTokenResolver().apply(tokenMaybe.get());
                if (resolve.isPresent() && resolve.get() instanceof DiceMathTerm dmt) {
                    return new TokenTerm(tokenMaybe.get(), dmt);
                }
            }
        }

        var coinFlipMaybe = CoinFlipTerm.parse(c);
        if (coinFlipMaybe.isPresent()) {
            string.advance();
            return coinFlipMaybe.get();
        }

        var stringMaybe = BasicExpressionParser.readNextString(string, '"', '\\')
                .or(() -> BasicExpressionParser.readNextString(string, '\'', '\\'));
        if (stringMaybe.isPresent()) {
            return new StringTerm(stringMaybe.get());
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
