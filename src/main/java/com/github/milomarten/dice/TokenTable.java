package com.github.milomarten.dice;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.ResolvedTokenTerm;
import com.github.milomarten.dice.term.TokenTerm;
import com.github.milomarten.evaluator.Term;
import com.github.milomarten.table.RandomlySelected;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A table of tokens in an easier format
 * A TokenTable can be passed into the EvaluationOptions and modified in a more intuitive
 * way compared to using a raw Function. The TokenTable supports two types of tokens:
 * 1. A dynamic token will return the result of a supplier when invoked. Can be invoked as a dice
 * roll (for example, if token `types` is defined as all Pokemon types, `2d@types` will return two random Pokemon types).
 * Note that a dynamic type MUST return a non-null value
 * 2. A constant token has a fixed value that will never vary. If invoked as a dice roll, it acts the same as if it's
 * value was provided (for example, if token `numTypes` is defined as 18, `2d@numTypes` acts identically to `2d18`.
 * <br>
 * The TokenTable supports an option for a parent table. If a token is not found, and a parent is provided, the
 * parent will be queried for the token instead.
 */
@AllArgsConstructor
@NoArgsConstructor
public class TokenTable implements Function<String, Optional<Term>> {
    private final Map<String, Supplier<? extends DiceMathTerm>> terms = new HashMap<>();
    private TokenTable parent;

    /**
     * Add a constant token to the table
     * @param name The name of the constant
     * @param term The value of the constant
     */
    public void addConstant(String name, DiceMathTerm term) {
        Objects.requireNonNull(term);
        this.terms.put(name, () -> new ResolvedTokenTerm(name, term));
    }

    /**
     * Add a dynamic token to the table
     * @param name The name of the token
     * @param supplier A function which will produce a Term when invoked
     */
    public void addDynamic(String name, Supplier<DiceMathTerm> supplier) {
        this.terms.put(name, () -> new TokenTerm(name, supplier));
    }

    /**
     * Add a dynamic token in the form of a RandomlySelected
     * A convenience method, this simply creates a dynamic token which invokes the RandomlySelected with
     * the provided randomness provider
     * @param name The name of the token
     * @param table The RandomlySelected to use
     * @param randomness The randomness provider
     */
    public void addRandomlySelected(String name, RandomlySelected<? extends DiceMathTerm> table, UniformRandomProvider randomness) {
        this.terms.put(name, () -> new TokenTerm(name, () -> table.get(randomness)));
    }

    @Override
    public Optional<Term> apply(String s) {
        if (!terms.containsKey(s)) {
            return (parent == null) ? Optional.empty() : parent.apply(s);
        } else {
            return Optional.of(terms.get(s).get());
        }
    }
}
