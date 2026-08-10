package com.github.milomarten.dice;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.parsing.StringExpressionEvaluator;
import org.junit.jupiter.api.Test;

class DiceExpressionParserTest {
    public static final StringExpressionEvaluator<DiceMathTerm> EVAL =
            new StringExpressionEvaluator<>(new DiceExpressionParser());

    @Test
    public void test() {
        var result = EVAL.evaluate("5d{10}+3");

        System.out.println(result);
    }
}