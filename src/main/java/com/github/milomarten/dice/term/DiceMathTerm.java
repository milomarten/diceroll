package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.Term;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The base class for any term permitted in a Dice expression
 */
public sealed interface DiceMathTerm extends Term permits CoinFlipTerm, ImplicitNumberTerm, NumberTerm, PlaceholderTerm, AbstractPoolTerm, PredicateTerm {
    /**
     * Convert this term into a BigDecimal
     * Most DiceMathTerm can be turned into a number, but this is not
     * a requirement. Anything that does not support being used as a number
     * should throw an ExpressionSyntaxError
     * @return This term's numerical value
     */
    BigDecimal asNumber();

    /**
     * Check if this term is numeric
     * @return True if numeric, false if something else
     */
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

    /**
     * Add this term to another.
     * By default, this and the other term are coerced into numbers, added together, and returned
     * as a new NumberTerm.
     * @param addend The value to add
     * @param options The evaluation options
     * @return A new term representing the sum of this and another
     */
    default DiceMathTerm add(DiceMathTerm addend, EvaluatorOptions options){
        return new NumberTerm(asNumber().add(addend.asNumber()));
    }

    /**
     * Subtract another term from this term.
     * By default, this and the other term are coerced into numbers, subtracted, and returned
     * as a new NumberTerm.
     * @param minuend The value to subtract
     * @param options The evaluation options
     * @return A new term representing the sum of this and another
     */
    default DiceMathTerm subtract(DiceMathTerm minuend, EvaluatorOptions options){
        return new NumberTerm(asNumber().subtract(minuend.asNumber()));
    }

    /**
     * Multiply this term by another.
     * By default, this and the other term are coerced into numbers, multiplied together, and returned
     * as a new NumberTerm.
     * @param multiplier The value to multiply
     * @param options The evaluation options
     * @return A new term representing the sum of this and another
     */
    default DiceMathTerm multiply(DiceMathTerm multiplier, EvaluatorOptions options){
        return new NumberTerm(asNumber().multiply(multiplier.asNumber()));
    }

    /**
     * Divides another term out of this term.
     * By default, this and the other term are coerced into numbers, divided, and returned
     * as a new NumberTerm. It is an error if divisor coerces into a 0.
     * @param divisor The value to divide
     * @param options The evaluation options
     * @return A new term representing the sum of this and another
     */
    default DiceMathTerm divide(DiceMathTerm divisor, EvaluatorOptions options){
        if (BigDecimal.ZERO.equals(divisor.asNumber())) {
            throw new ExpressionSyntaxError("Division by Zero");
        }

        return new NumberTerm(asNumber().divide(divisor.asNumber(), options.getRoundingMode()));
    }

    /**
     * Takes the root of another term, using this term as an index.
     * This term is coerced into an integer, and the other term into any number. It is an error if this
     * term coerces to a non-positive number.
     * @param radicand The term to root
     * @param options The evaluation options
     * @return A new term representing the nth root of the other term
     */
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

    /**
     * Drop some number of items.
     * By default, this throws an exception
     * @param lowest If true, the lowest should be dropped, otherwise the highest should
     * @param quantity The number of items to drop
     * @param options The evaluation options
     * @return A new term representing this term with some items dropped.
     */
    default DiceMathTerm drop(boolean lowest, DiceMathTerm quantity, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("drop " + (lowest ? "lowest" : "highest") + " unsupported");
    }

    /**
     * Keep some number of items, dropping the rest.
     * By default, this throws an exception
     * @param lowest If true, the lowest should be kept, otherwise the highest should
     * @param quantity The number of items to keep
     * @param options The evaluation options
     * @return A new term representing this term with some items dropped.
     */
    default DiceMathTerm keep(boolean lowest, DiceMathTerm quantity, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("keep " + (lowest ? "lowest" : "highest") + " unsupported");
    }

    /**
     * Explode any items matching a predicate.
     * By default, this throws an exception
     * @param predicate The predicate to use to determine if an explosion occurs
     * @param options The evaluation options
     * @return A new term representing this term after exploding.
     */
    default DiceMathTerm explode(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("explode unsupported");
    }

    /**
     * Reroll any items matching a predicate.
     * By default, this throws an exception. The user expectation is that rerolls will keep occurring if any roll
     * matches the predicate (within safety bounds)
     * @param predicate The predicate to use to determine if a reroll occurs
     * @param options The evaluation options
     * @return A new term representing this term after rerolling.
     */
    default DiceMathTerm reroll(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("reroll unsupported");
    }

    /**
     * Reroll any items matching a predicate.
     * By default, this throws an exception. The user expectation is that only one round of reroll occurs.
     * @param predicate The predicate to use to determine if a reroll occurs
     * @param options The evaluation options
     * @return A new term representing this term after rerolling.
     */
    default DiceMathTerm rerollOnce(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("reroll once unsupported");
    }

    /**
     * Count successes based on some predicate.
     * By default, this throws an exception.
     * @param predicate The predicate to use to determine a success
     * @param options The evaluation options
     * @return A new term representing this term with successes counted
     */
    default DiceMathTerm targetSuccess(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("target success unsupported");
    }

    /**
     * Count failures based on some predicate.
     * By default, this throws an exception.
     * @param predicate The predicate to use to determine a failure
     * @param options The evaluation options
     * @return A new term representing this term with failues counted in the negative
     */
    default DiceMathTerm targetFailure(DiceMathTerm predicate, EvaluatorOptions options) {
        throw new ExpressionSyntaxError("target failure unsupported");
    }

}
