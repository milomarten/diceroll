package com.github.milomarten.dice;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.NumberTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.formatting.LineByLineFormatter;
import com.github.milomarten.parsing.StringExpressionEvaluator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

class DiceExpressionParserTest {
    public static final StringExpressionEvaluator<DiceMathTerm> EVAL =
            new StringExpressionEvaluator<>(new DiceExpressionParser());

    @Test
    public void test() {
        var expr = "3d@ten";
        var result = EVAL.evaluate(expr, eo());
        var formatted = LineByLineFormatter.format(result, new DiceResultFormatter());

        System.out.println(expr);
        formatted.forEach(line -> System.out.println("- " + line));
    }

    private EvaluatorOptions eo() {
        return EvaluatorOptions.builder()
                .tokenResolver((token) -> {
                   return Optional.of(new NumberTerm(BigDecimal.TEN));
                })
                .build();
    }
}