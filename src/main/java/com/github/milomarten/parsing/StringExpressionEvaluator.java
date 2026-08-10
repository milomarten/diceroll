package com.github.milomarten.parsing;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionEvaluator;
import com.github.milomarten.evaluator.Term;
import com.github.milomarten.evaluator.ValueAndExpression;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

/**
 * An Expression Evaluator which parses a string into an Expression and evaluates it.
 * At its core, this class will iterate left-to-right through a string, pulling out
 * terms or operations, and using an ExpressionEvaluator to compute a result. For any failure
 * in evaluation, an ExpressionSyntaxError is thrown.
 * <br>
 * This class depends on an instance of TermParser, which pulls out, parses, and applies the next token.
 * @param <T> The type of Term
 */
@RequiredArgsConstructor
public class StringExpressionEvaluator<T extends Term> {
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private final TermParser<T> parser;

    public ValueAndExpression<T> evaluate(String expression, EvaluatorOptions options) {
        var evaluator = new ExpressionEvaluator<T>(options);
        var string = new ShrinkingString(SPACES.matcher(expression).replaceAll(""));

        do {
            parser.parseNextToken(string, options, evaluator);
        } while (!string.isEmpty());

        return evaluator.finish();
    }

    public ValueAndExpression<T> evaluate(String expression) {
        return evaluate(expression, EvaluatorOptions.builder().build());
    }
}
