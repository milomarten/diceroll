package com.github.milomarten.dice.term;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;

import java.math.BigDecimal;
import java.util.function.Supplier;

/**
 * A term which indicates an unresolved token
 * At this point, a token with a specific name exists, but has not yet been resolved into a concrete value. Once
 * an operator attempts to read it, it is turned into a ResolvedTokenTerm with the true value. The
 * only exception is when a TokenTerm is provided as a dice face parameter, in which case the TokenTerm will continually
 * generate new values each time it is called.
 * All operations will fail with an ExpressionSyntaxError. Operation code should explicitly
 * handle TokenTerms, either by calling resolve() and replacing it, or safely encapsulating the
 * behavior in another object.
 * @param name The name of the token
 * @param wrapped A function which will create a DiceMathTerm generically
 */
public record TokenTerm(String name, Supplier<? extends DiceMathTerm> wrapped) implements DiceMathTerm {
    public static final String TOKEN_RESOLVE_OPERATION = "TOKEN";

    /**
     * Resolve this token into a final value
     * @return A ResolvedTokenTerm with this name, and the invokation of the wrapped method
     */
    public ResolvedTokenTerm resolve() {
        return new ResolvedTokenTerm(name, wrapped.get());
    }

    @Override
    public BigDecimal asNumber() {
        throw new ExpressionSyntaxError("Tried to resolve a TokenTerm as a number");
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public String asString() {
        throw new ExpressionSyntaxError("Tried to resolve a TokenTerm as a string");
    }
}