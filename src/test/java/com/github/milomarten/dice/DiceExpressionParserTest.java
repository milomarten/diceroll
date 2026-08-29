package com.github.milomarten.dice;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.formatting.LineByLineFormatter;
import com.github.milomarten.parsing.StringExpressionEvaluator;
import org.junit.jupiter.api.Test;

class DiceExpressionParserTest {
    public static final StringExpressionEvaluator<DiceMathTerm> EVAL =
            new StringExpressionEvaluator<>(new DiceExpressionParser());

    @Test
    public void test() {
        var expr = "1d({1,3,5,7}+2)";
        var result = EVAL.evaluate(expr);
        var formatted = LineByLineFormatter.format(result, new DiceResultFormatter());

        System.out.println(expr);
        formatted.forEach(line -> System.out.println("- " + line));
    }
}