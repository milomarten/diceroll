package com.github.milomarten.dice.term;

import lombok.experimental.Delegate;

/**
 * The result of resolving a token into a fixed value
 * At this point, a token with a specific name exists, AND has been resolved into a concrete value. This is either
 * by using a constant token, or by resolving a TokenTerm manually.
 * This term acts identically to the wrapped term in all operational aspects. This class solely serves to
 * maintain the token's name for formatting purposes.
 * @param name The token's name
 * @param wrapped The resolved value of the token
 */
public record ResolvedTokenTerm(String name, @Delegate DiceMathTerm wrapped) implements DiceMathTerm {
}
