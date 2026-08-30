package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;

import java.math.BigDecimal;

public record StringTerm(String value) implements DiceMathTerm {
    @Override
    public BigDecimal asNumber() {
        throw new ExpressionSyntaxError("Tried to read a string as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public DiceMathTerm add(DiceMathTerm addend, EvaluatorOptions options) {
        if (addend instanceof StringTerm(String other)) {
            return new StringTerm(value + other);
        } else if (addend.isNumber()) {
            return new StringTerm(value + addend.asNumber().toPlainString());
        }
        throw new ExpressionSyntaxError("Tried to add a string and a number");
    }
}
