package com.github.milomarten.dice;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.parsing.StringExpressionEvaluator;
import org.junit.jupiter.api.Test;

class DiceExpressionParserTest {
    private static final StringExpressionEvaluator<DiceMathTerm> e =
            new StringExpressionEvaluator<>(new DiceExpressionParser());

    @Test
    public void test() {
        var expression = "d(1,3,5)";
        var finished = e.evaluate(expression);

        System.out.println(finished.s());
        if (finished.value().isNumber()) {
            System.out.println("\t= " + finished.value().asNumber());
        }
    }
}