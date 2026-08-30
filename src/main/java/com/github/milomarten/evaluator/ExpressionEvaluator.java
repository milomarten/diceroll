package com.github.milomarten.evaluator;

import lombok.Getter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Supports programmatic expression evaluation.
 * Terms can be combined with Operations, in the same order as standard
 * infix notation. Order of operations is respected, and enforcement of order between
 * terms and operations.
 * If anything unexpected is present, an ExpressionSyntaxError is thrown and processing halts.
 * @param <T> The type of term
 */
public class ExpressionEvaluator<T extends Term> {
    private final TermStack<T> terms;
    private final Deque<OperationOrParenthesis<T>> operators = new LinkedList<>();
    private final EvaluatorOptions options;

    @Getter private boolean expectingTerm = true;

    public ExpressionEvaluator(EvaluatorOptions options) {
        this.terms = new TermStack<>(options);
        this.options = options;
    }

    public ExpressionEvaluator() {
        this(EvaluatorOptions.builder().build());
    }

    /**
     * Push a term to the stack.
     * @param term The term to push
     */
    public void push(T term) {
        if (!expectingTerm) {
            throw new ExpressionSyntaxError("Was not expecting term " + term);
        }
        this.terms.push(term);
        this.expectingTerm = false;
    }

    /**
     * Push an Operation to the stack
     * Pushing an Operation may result in the stack changing, as operations with
     * lower priority are activated and new terms are calculated.
     * <br>
     * Some operators support implicit left or right terms, if none are specified. If an Operator is pushed
     * and a term was expected, it checks the previous operations implicit right term (if exists), and the
     * pushed operator's implicit left term. If either exist, that term is pushed first. If both exist, the
     * implicit right Operator takes precedence
     * @param operator The operator to push
     */
    public void push(Operation<T> operator) {
        if (expectingTerm) {
            var leftTerm = operator.getImplicitLeftTerm();
            if (leftTerm != null) {
                this.terms.pushInternal(new ValueAndExpression<>(leftTerm));
            } else {
                throw new ExpressionSyntaxError("Missing term for operator " + operator);
            }
        }

        Operation<T> underneath;
        while (!operators.isEmpty() &&
                operators.peek().isOperation() &&
                operators.peek().getPriority() <= operator.getPriority()) {
            underneath = operators.pop().getOperation();
            var result = underneath.evaluate(terms, this.options);
            this.terms.pushInternal(result);
        }

        operators.push(new OperationWrapper<>(operator));
        expectingTerm = operator.expectTermAfter();
    }

    /**
     * Push the start of a bounded operation.
     * The start of a bounded operation acts like a Term, and so can be used whenever you would
     * push a Term.
     * @param boundedOperation The BoundedOperation
     */
    public void pushBoundedOperationStart(BoundedOperation<T> boundedOperation) {
        if (expectingTerm) {
            operators.push(new ParenthesisWrapper<>(boundedOperation));
        } else {
            throw new ExpressionSyntaxError("Incorrectly started bounded operation " + boundedOperation.getLeftBound());
        }
    }

    /**
     * Push the end of a bounded operation.
     * The stack is popped and evaluated until the matching BoundedOperation is found. In contrast with the
     * BoundedOperationStart, the end of a bounded operation acts like an Operation, and can be used whenever
     * an Operation is expected.
     * An ExpressionSyntaxError is thrown if no matching BoundedOperation is found, or if an unmatched
     * BoundedOperation is found instead.
     * @param right The right-bound character.
     */
    public void pushBoundedOperationEnd(String right) {
        Operation<T> underneath;
        while (!operators.isEmpty() &&
                operators.peek().isOperation()) {
            underneath = operators.pop().getOperation();
            var result = underneath.evaluate(terms, this.options);
            this.terms.pushInternal(result);
        }
        var assumedParenthesis = operators.peek();
        if (assumedParenthesis instanceof ExpressionEvaluator.ParenthesisWrapper<T> par) {
            if (Objects.equals(right, par.oper.getRightBound())) {
                var result = par.oper.evaluate(terms, options);
                this.terms.pushInternal(result);
                operators.pop();
                expectingTerm = false;
                return;
            }
        }
        throw new ExpressionSyntaxError("Mismatched parenthesis");
    }

    /**
     * Complete evaluation and return the final answer
     * The operation stack is popped until there's only one term remaining. This
     * can be done at any time the Evaluator expects an operation.
     * @return The result of evaluating the Expression
     */
    public ValueAndExpression<T> finish() {
        while(!operators.isEmpty()) {
            var op = operators.pop().getOperation();
            if (op == null) {
                throw new ExpressionSyntaxError("Mismatched parenthesis");
            }
            var result = op.evaluate(terms, this.options);
            this.terms.pushInternal(result);
        }

        if (terms.size() != 1) {
            throw new ExpressionSyntaxError("Mismatched operations");
        }

        return terms.pop();
    }

    private sealed interface OperationOrParenthesis<T extends Term>
     permits OperationWrapper, ParenthesisWrapper {
        Operation<T> getOperation();
        boolean isOperation();
        int getPriority();
    }

    private record OperationWrapper<T extends Term>(Operation<T> oper) implements OperationOrParenthesis<T> {
        @Override
        public Operation<T> getOperation() {
            return oper;
        }

        @Override
        public boolean isOperation() {
            return true;
        }

        @Override
        public int getPriority() {
            return oper.getPriority();
        }
    }

    private record ParenthesisWrapper<T extends Term>(BoundedOperation<T> oper) implements OperationOrParenthesis<T> {
        @Override
        public Operation<T> getOperation() {
            throw new ExpressionSyntaxError("Tried to unwrap parenthesis as an operation");
        }

        @Override
        public boolean isOperation() {
            return false;
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }
    }
}
