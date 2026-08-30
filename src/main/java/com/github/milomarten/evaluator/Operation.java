package com.github.milomarten.evaluator;

/**
 * An Operation which acts on some quantity of Terms.
 * The most familiar Operations, such as addition or multiplication, consume two terms and output one.
 * However, there are also unary operations, which consume one term and output one. Of the unary
 * operators, Operators can either be prefix (such as square root) or postfix (such as factorial).
 * @param <T> The type of Term this Operation can act on
 */
public interface Operation<T extends Term> {
    /**
     * Evaluate this operation, giving the entire stack as context.
     * Keep in mind the termStack is "upside-down"... For a binary function like multiplication,
     * The first "pop" returns the *second* term, and the second "pop" returns the *first* term.
     * <br>
     * The returned ValueAndExpression should contain both the real result, and a string representation of the process.
     * The actual visualization is up to the implementor, but is typically the concatenation of calling s() on all related terms,
     * and putting the String representation of the Operation in the relevant spot relative to them
     * @param termStack The stack of terms
     * @param options The options passed to the initial evaluator
     * @return The result of the operation, both as a term and as a displayable string.
     */
    ValueAndExpression<T> evaluate(TermStack<T> termStack, EvaluatorOptions options);

    /**
     * The priority of this operation.
     * Possibly confusingly, lower-numbered operations are executed before higher-numbered ones.
     * For instance, in normal mathematics, multiplication occurs before addition. Thus, a multiplication operation
     * should have a lower priority number than addition.
     * @return A number which represents the order of operations.
     */
    int getPriority();

    /**
     * Get an implicit left term of this operation.
     * Some Operations have an assumed "left" term. As an example, √ implicitly assumes the square root, so the
     * assumed left term would be 2 if none is otherwise specified.
     * Since binary operators are more common than ones with an implicit left side, this function defaults
     * to null (indicating there is no implicit term).
     * @return The implicit left term of this Operation, if one is not otherwise provided.
     */
    default T getImplicitLeftTerm() {
        return null;
    }

    /**
     * Check if this operation expects a term after it or not
     * This should return true for binary operators, but false for postfix
     * operations
     * @return True if a term should follow this operation, false otherwise.
     */
    default boolean expectTermAfter() { return true; }
}
