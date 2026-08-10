package com.github.milomarten.dice;

import com.github.milomarten.dice.operation.DiceOperation;
import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.NumberTerm;
import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.ExpressionEvaluator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class DiceOperationTest {
    @Test
    public void testRoll() {
        var eval = new ExpressionEvaluator<DiceMathTerm>();
        eval.push(new NumberTerm(BigDecimal.TEN));
        eval.push(DiceOperation.DICE);
        eval.pushBoundedOperationStart(new BoundedOperation.Brackets<>('(', ')'));
        eval.push(NumberTerm.ONE);
        eval.push(DiceOperation.COMMA);
        eval.push(new NumberTerm(BigDecimal.TEN));
        eval.pushBoundedOperationEnd(")");
        eval.push(DiceOperation.KEEP_LOWEST);

        var finished = eval.finish();
        System.out.println(finished.s());
        System.out.println("\t= " + finished.value());
    }
}