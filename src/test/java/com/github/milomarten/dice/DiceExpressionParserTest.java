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
        var expr = "(2d6+2)*(d10+1)";
        var result = EVAL.evaluate(expr);
        var formatted = LineByLineFormatter.format(result);

        System.out.println(expr);
        formatted.forEach(line -> System.out.println("- " + line));

//        if (result.value().isNumber()) {
//            System.out.println("= " + result.value().asNumber());
//        }
    }
}