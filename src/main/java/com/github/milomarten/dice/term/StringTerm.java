package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;

import java.math.BigDecimal;

/**
 * A term which represents a string.
 * A String is never a number, and using it as one will always fail.
 * The only supported operations are:
 * - Addition will concat this string with the other term as a string. The second term can be a string
 * or any term that can turn into a number.
 * - Multiply will repeat the string a number of times equal to the multiplier.
 * @param value The value, as a string
 */
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
    public String asString() {
        return value;
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

    @Override
    public DiceMathTerm multiply(DiceMathTerm multiplier, EvaluatorOptions options) {
        if (multiplier.isNumber()) {
            var repeater = multiplier.asInteger(options);
            if (repeater < 0) {
                throw new ExpressionSyntaxError("Tried to multiply a string by a negative number");
            }
            return new StringTerm(value.repeat(repeater));
        }
        throw new ExpressionSyntaxError("Tried to multiply a string and a non-number");
    }
}
