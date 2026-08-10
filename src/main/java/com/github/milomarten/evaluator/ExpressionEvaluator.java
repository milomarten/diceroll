package com.github.milomarten.evaluator;

import lombok.Getter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

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

    public void push(T term) {
        if (!expectingTerm) {
            throw new ExpressionSyntaxError("Was not expecting term " + term);
        }
        this.terms.push(term);
        this.expectingTerm = false;
    }

    public void push(Operation<T> operator) {
        if (expectingTerm) {
            var leftTerm = operator.getImplicitLeftTerm();
            var previousOperation = operators.isEmpty() ? null : operators.peek();
            var rightTerm = (previousOperation != null && previousOperation.isOperation()) ? previousOperation.getOperation().getImplicitRightTerm() : null;
            if (rightTerm != null) {
                this.terms.pushInternal(new ValueAndExpression<>(rightTerm, ""));
            } else if (leftTerm != null) {
                this.terms.pushInternal(new ValueAndExpression<>(leftTerm, ""));
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

    public void pushBoundedOperationStart(BoundedOperation<T> boundedOperation) {
        if (expectingTerm) {
            operators.push(new ParenthesisWrapper<>(boundedOperation));
        } else {
            throw new ExpressionSyntaxError("Incorrectly started bounded operation " + boundedOperation.getLeftBound());
        }
    }

    public void pushBoundedOperationEnd(String right) {
        if (expectingTerm) {
            var previousOperation = operators.isEmpty() ? null : operators.peek();
            var rightTerm = (previousOperation != null && previousOperation.isOperation()) ? previousOperation.getOperation().getImplicitRightTerm() : null;
            if (rightTerm == null) {
                throw new ExpressionSyntaxError("Unfinished operation");
            } else {
                this.terms.pushInternal(new ValueAndExpression<>(rightTerm, ""));
            }
        }

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

    public ValueAndExpression<T> finish() {
        if (expectingTerm) {
            var previousOperation = operators.isEmpty() ? null : operators.peek();
            var rightTerm = (previousOperation != null && previousOperation.isOperation()) ? previousOperation.getOperation().getImplicitRightTerm() : null;
            if (rightTerm == null) {
                throw new ExpressionSyntaxError("Unfinished operation");
            } else {
                this.terms.pushInternal(new ValueAndExpression<>(rightTerm, ""));
            }
        }

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
