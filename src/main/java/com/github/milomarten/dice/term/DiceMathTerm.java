package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.Term;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The base class for any term permitted in a Dice expression
 */
public sealed interface DiceMathTerm extends Term permits CoinFlipTerm, ImplicitNumberTerm, NumberTerm, PlaceholderTerm, PoolTerm, PredicateTerm {
    /**
     * Convert this term into a BigDecimal
     * Most DiceMathTerm can be turned into a number, but this is not
     * a requirement. Anything that does not support being used as a number
     * should throw an ExpressionSyntaxError
     * @return This term's numerical value
     */
    BigDecimal asNumber();

    boolean isNumber();

    /**
     * Convert this term into an integer.
     * The BigDecimal representation of this term (rounded based on the rounding mode) is converted to
     * an int. If the result overflows an int, an ExpressionSyntaxError is returned.
     * This method should be used if, and only if, an integer is the ONLY thing that makes sense for an Operation.
     * For instance, it is not possible to roll a d10.5, as a dn can only roll integers.
     * @param options The evaluation options, containing the rounding mode.
     * @return This term, expressed as an integer.
     */
    default int asInteger(EvaluatorOptions options) {
        var n = asNumber();
        try {
            return n.round(new MathContext(0, options.getRoundingMode())).intValueExact();
        } catch (ArithmeticException ex) {
            throw new ExpressionSyntaxError("Number " + n + " is too large to be treated as an integer");
        }
    }

    default DiceMathTerm add(DiceMathTerm addend, EvaluatorOptions options){
        return new NumberTerm(asNumber().add(addend.asNumber()));
    }

    default DiceMathTerm subtract(DiceMathTerm minuend, EvaluatorOptions options){
        return new NumberTerm(asNumber().subtract(minuend.asNumber()));
    }

    default DiceMathTerm multiply(DiceMathTerm multiplier, EvaluatorOptions options){
        return new NumberTerm(asNumber().multiply(multiplier.asNumber()));
    }

    default DiceMathTerm divide(DiceMathTerm divisor, EvaluatorOptions options){
        if (BigDecimal.ZERO.equals(divisor.asNumber())) {
            throw new ExpressionSyntaxError("Division by Zero");
        }

        return new NumberTerm(asNumber().divide(divisor.asNumber(), options.getRoundingMode()));
    }

    default DiceMathTerm root(DiceMathTerm radicand, EvaluatorOptions options){
        var n = asInteger(options);
        var x = radicand.asNumber();

        if (n == 1) {
            return radicand;
        } else if (n == 2) {
            try {
                var sqrt = x.sqrt(MathContext.DECIMAL128);
                return new NumberTerm(sqrt);
            } catch (ArithmeticException ex) {
                throw new ExpressionSyntaxError("Root operation did not work as expected. The radicand was probably negative");
            }
        } else if (n > 0) {
            var power = 1d / n;
            var result = Math.pow(x.doubleValue(), power);
            if (Double.isFinite(result)) {
                return new NumberTerm(BigDecimal.valueOf(result));
            } else {
                throw new ExpressionSyntaxError("Root operation did not work as expected. The radicand was probably negative");
            }
        } else {
            throw new ExpressionSyntaxError("Can't evaluate a root if the index is nonpositive");
        }
    }

    default DiceMathTerm drop(boolean lowest, DiceMathTerm quantity, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("drop " + (lowest ? "lowest" : "highest") + " unsupported");
    }

    default DiceMathTerm keep(boolean lowest, DiceMathTerm quantity, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("keep " + (lowest ? "lowest" : "highest") + " unsupported");
    }

    default DiceMathTerm explode(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("explode unsupported");
    }

    default DiceMathTerm reroll(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("reroll unsupported");
    }

    default DiceMathTerm rerollOnce(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("reroll once unsupported");
    }

    default DiceMathTerm targetSuccess(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("target success unsupported");
    }

    default DiceMathTerm targetFailure(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("target failure unsupported");
    }

}
