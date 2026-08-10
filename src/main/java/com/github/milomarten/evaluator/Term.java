package com.github.milomarten.evaluator;

/**
 * Represents a term in an expression
 * A Term is anything that can be modified using an Operation (and possibly other Terms) to create another
 * Term.
 */
public interface Term {
    /**
     * Get this term as a string.
     * I do this instead of toString, since toString may contain info for debugging.
     * The result of calling this method should be something client-friendly.
     * By default, this simply calls toString, for convenience.
     * @return This term as a friendly string
     */
    default String asString() {
        return toString();
    }
}
