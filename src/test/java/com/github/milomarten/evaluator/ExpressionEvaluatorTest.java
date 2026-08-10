package com.github.milomarten.evaluator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorTest {
    private static final BoundedOperation<NumberTerm> PARENTHESIS = new BoundedOperation.Brackets<>('(', ')');

    private record NumberTerm(int integer) implements Term { }

    private static class Add implements Operation<NumberTerm> {
        @Override
        public ValueAndExpression<NumberTerm> evaluate(TermStack<NumberTerm> termStack, EvaluatorOptions options) {
            var term2 = termStack.pop();
            var term1 = termStack.pop();
            var sum = term1.value().integer + term2.value().integer;
            return new ValueAndExpression<>(new NumberTerm(sum));
        }

        @Override
        public int getPriority() {
            return 2;
        }
    }

    private static class Multiply implements Operation<NumberTerm> {
        @Override
        public ValueAndExpression<NumberTerm> evaluate(TermStack<NumberTerm> termStack, EvaluatorOptions options) {
            var term2 = termStack.pop();
            var term1 = termStack.pop();
            var sum = term1.value().integer * term2.value().integer;
            return new ValueAndExpression<>(new NumberTerm(sum));
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    @Test
    public void testSimpleSum() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(2));

        var finished = evaluator.finish();
        assertEquals(7, finished.value().integer);
    }

    @Test
    public void testLargerSum() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(2));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(3));

        var finished = evaluator.finish();
        assertEquals(10, finished.value().integer);
    }

    @Test
    public void testLargerSumWithParenthesis() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.pushBoundedOperationStart(PARENTHESIS);
        evaluator.push(new NumberTerm(2));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(3));
        evaluator.pushBoundedOperationEnd(")");

        var finished = evaluator.finish();
        assertEquals(10, finished.value().integer);
    }

    @Test
    public void testMissingRightParenthesis() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.pushBoundedOperationStart(PARENTHESIS);
        evaluator.push(new NumberTerm(2));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(3));

        assertThrows(ExpressionSyntaxError.class, evaluator::finish);
    }

    @Test
    public void testMissingLeftParenthesis() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(2));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(3));

        assertThrows(ExpressionSyntaxError.class, () -> evaluator.pushBoundedOperationEnd(")"));
    }

    @Test
    public void testMismatchedParenthesis() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.pushBoundedOperationStart(PARENTHESIS);
        evaluator.push(new NumberTerm(2));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(3));

        assertThrows(ExpressionSyntaxError.class, () -> evaluator.pushBoundedOperationEnd("]"));
    }

    @Test
    public void testOrderOfOperations() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(2));
        evaluator.push(new Multiply());
        evaluator.push(new NumberTerm(3));

        var finished = evaluator.finish();
        assertEquals(11, finished.value().integer);
    }

    @Test
    public void testBypassOrderOfOperationsWithParenthesis() {
        var evaluator = new ExpressionEvaluator<NumberTerm>();
        evaluator.pushBoundedOperationStart(PARENTHESIS);
        evaluator.push(new NumberTerm(5));
        evaluator.push(new Add());
        evaluator.push(new NumberTerm(2));
        evaluator.pushBoundedOperationEnd(")");
        evaluator.push(new Multiply());
        evaluator.push(new NumberTerm(3));

        var finished = evaluator.finish();
        assertEquals(21, finished.value().integer);
    }
}