package com.github.milomarten.dice.operation;

import com.github.milomarten.dice.die.*;
import com.github.milomarten.dice.term.*;
import com.github.milomarten.evaluator.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * All standard Operations which are used in dicerolls.
 */
@RequiredArgsConstructor
@Getter
public enum DiceOperation implements Operation<DiceMathTerm> {
    /**
     * Add two terms.
     * Both sides are coerced into numbers and added
     */
    ADD("+", 10){
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "addend", "addend", DiceMathTerm::add);
        }
    },
    /**
     * Subtract two terms.
     * Both sides are coerced into numbers and subtracted
     */
    SUBTRACT("-", 10) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "minuend", "subtrahend", DiceMathTerm::subtract);
        }
    },
    /**
     * Multiply two terms.
     * Both sides are coerced into numbers and multiplied
     */
    MULTIPLY("*", 8) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "multiplicand", "multiplier", DiceMathTerm::multiply);
        }
    },
    /**
     * Divide two terms.
     * Both sides are coerced into numbers and divided
     */
    DIVIDE("/", 8) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "dividend", "divisor", DiceMathTerm::divide);
        }
    },
    /**
     * Square root.
     * This one's for you, Grapha
     * Both sides are coerced into numbers and added. The left side is rounded to an integer according to the rounding rules,
     * and cannot be less than 1.
     * If the left side is omitted, 2 is assumed (square root)
     */
    ROOT("√", 6) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "index", "radicand", DiceMathTerm::root);
        }

        @Override
        public DiceMathTerm getImplicitLeftTerm() {
            return ImplicitNumberTerm.TWO;
        }
    },
    /**
     * Comma operator
     * This allows creation of a Pool. If the left side is a pool, the right side is added to it.
     * If the left side is not a pool, the two sides are combined into a new pool.
     */
    COMMA(",", 20) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            var newEntry = DiceOperation.pull(termStack, "pool entry");
            var numberOrPool = DiceOperation.pull(termStack, "num of dice");

            if (numberOrPool.value() instanceof PoolTerm pt) {
                pt.addToPool(newEntry.value());
                var newChildren = new ArrayList<>(numberOrPool.children());
                newChildren.add(newEntry);
                return new ValueAndExpression<>(pt, this, newChildren);
            } else {
                var newPool = new PoolTerm(numberOrPool.value(), newEntry.value());
                return new ValueAndExpression<>(newPool, this, List.of(numberOrPool, newEntry));
            }
        }
    },
    // Dice Shit
    /**
     * Create the result of a dice roll.
     * The left side is coerced into an integer, using the rounding rules. The type of dice depends on the right term:
     * - If a pool is provided, the faces of the dice are each element in the pool. For example, 1d{2,4,6,8} has an equal
     * chance of rolling a 2, 4, 6, or 8. The result can be coerced into a number iff each possibility is a number.
     * - If C, H, or T is provided, the dice becomes a coin toss between H and T. The result can never be coerced
     * into a number without using a counting operator.
     * - For any other term, the result is coerced into an integer using rounding rules. The result is any integer
     * between 1 and that integer, equally distributed. If the integer is less than 1, an error is thrown. The result
     * can always be coerced into a number.
     * <br>
     * In all cases, attempting to roll a negative amount of times will throw an error.
     * <br>
     * If the left side is omitted, 1 is assumed
     */
    DICE("d", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            var numSides = DiceOperation.pull(termStack, "number of sides");
            var numDice = DiceOperation.pull(termStack, "num of dice");

            var numDiceInt = numDice.value().asInteger(options);

            Die<DiceMathTerm> die;
            if (numSides.value() instanceof PoolTerm pool) {
                die = new PoolDie(pool, options.getRandomSource());
            } else if (numSides.value() instanceof CoinFlipTerm) {
                die = new DiscreteDie(options.getRandomSource(), CoinFlipTerm.HEADS, CoinFlipTerm.TAILS);
            } else {
                die = new NDie(numSides.value().asInteger(options), options.getRandomSource());
            }
            var resultant = new DieResultTerm(die, numDice.value(), die.roll(numDiceInt, options));

            return new ValueAndExpression<>(resultant, this, List.of(numDice, numSides));
        }

        @Override
        public DiceMathTerm getImplicitLeftTerm() {
            return ImplicitNumberTerm.ONE;
        }
    },
    /**
     * Drop the lowest n elements in a roll or pool.
     * The right side is coerced to an integer according to the rounding rules. If the left side is a pool or dice
     * roll, the lowest [right] elements are dropped, and no longer count toward further computation. This operator
     * is undefined for any other left side, and will fail iff a Term in the pool or dice roll is not a number.
     * If the right side is omitted, 1 is assumed (drop the lowest element)
     */
    DROP_LOWEST("dl", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "pool", "num to drop",
                    (one, two, opts) -> one.drop(true, two, options));
        }
    },
    /**
     * Drop the highest n elements in a roll or pool.
     * The right side is coerced to an integer according to the rounding rules. If the left side is a pool or dice
     * roll, the highest [right] elements are dropped, and no longer count toward further computation. This operator
     * is undefined for any other left side, and will fail iff a Term in the pool or dice roll is not a number.
     * If the right side is omitted, 1 is assumed (drop the highest element)
     */
    DROP_HIGHEST("dh", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "pool", "num to drop",
                    (one, two, opts) -> one.drop(false, two, options));
        }
    },
    /**
     * Keep the lowest n elements in a roll or pool.
     * The right side is coerced to an integer according to the rounding rules. If the left side is a pool or dice
     * roll, all but the lowest [right] elements are dropped, and no longer count toward further computation. This operator
     * is undefined for any other left side, and will fail iff a Term in the pool or dice roll is not a number.
     * If the right side is omitted, 1 is assumed (keep the lowest element)
     */
    KEEP_LOWEST("kl", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "pool", "num to keep",
                    (one, two, opts) -> one.keep(true, two, options));
        }
    },
    /**
     * Keep the highest n elements in a roll or pool.
     * The right side is coerced to an integer according to the rounding rules. If the left side is a pool or dice
     * roll, all but the highest [right] elements are dropped, and no longer count toward further computation. This operator
     * is undefined for any other left side, and will fail iff a Term in the pool or dice roll is not a number.
     * If the right side is omitted, 1 is assumed (keep the highest element)
     */
    KEEP_HIGHEST("kh", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "pool", "num to keep",
                    (one, two, opts) -> one.keep(false, two, options));
        }
    },
    /**
     * Explode the dice in a roll.
     * Of the dice that were rolled, any matching a certain predicate will cause another dice to be rolled. This continues
     * until no more dice match the predicate. To avoid abuse, the explosion limit is 20, at which point no
     * more explosions occur regardless of matching.
     * The left side must be a dice roll. The right side can be any of the following:
     * 1. If no predicate is provided, the highest dice value will explode. This will fail iff any dice face is non-numeric
     * 2. >[number] will explode any dice greater than or equal to number. This will fail iff any dice face is non-numeric
     * 3. <[number] will explode any dice less than or equal to number. This will fail iff any dice face is non-numeric
     * 4. =[number] will explode any dice equal to value. This will fail iff any dice face is non-numeric
     * 5. If a pool is provided, the dice will explode if any match a value in the pool. This will always work.
     * 6. In any other case, the dice will explode if it equals() the term provided. This will always work.
     * If the right side is omitted, highest dice value is assumed.
     */
    EXPLODE("!", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "dice pool", "explosion predicate",
                    (one, two, opts) -> one.explode(two, options));
        }
    },
    /**
     * Reroll the dice in a roll.
     * Of the dice that were rolled, any matching a certain predicate will be dropped and rolled again. This continues
     * until no more dice match the predicate. To avoid abuse, the explosion limit is 20, at which point no
     * more rerolls occur regardless of matching.
     * The left side must be a dice roll. The right side can be any of the following:
     * 2. >[number] will reroll any dice greater than or equal to number. This will fail iff any dice face is non-numeric
     * 3. <[number] will reroll any dice less than or equal to number. This will fail iff any dice face is non-numeric
     * 4. =[number] will reroll any dice equal to value. This will fail iff any dice face is non-numeric
     * 5. If a pool is provided, the dice will reroll if any match a value in the pool. This will always work.
     * 6. In any other case, the dice will reroll if it equals() the term provided. This will always work.
     */
    REROLL("r", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "dice pool", "reroll predicate",
                    (one, two, opts) -> one.reroll(two, options));
        }
    },
    /**
     * Reroll the dice in a roll, but only once.
     * This is identical to REROLL, but only one iteration of rerolling occurs. All the same restrictions apply
     * otherwise.
     */
    REROLL_ONCE("ro", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "dice pool", "reroll predicate",
                    (one, two, opts) -> one.rerollOnce(two, options));
        }
    },
    /**
     * Switch to counting mode
     * The dice roll or pool is switched to a counting mode, which counts elements matching a certain predicate.
     * The right side can be any of the following:
     * 2. >[number] will count any dice greater than or equal to number. This will fail iff any dice face is non-numeric
     * 3. <[number] will count any dice less than or equal to number. This will fail iff any dice face is non-numeric
     * 4. =[number] will count any dice equal to value. This will fail iff any dice face is non-numeric
     * 5. If a pool is provided, the dice will count if any match a value in the pool. This will always work.
     * 6. In any other case, the dice will count if it equals() the term provided. This will always work.
     */
    TARGET_SUCCESS("s", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "pool", "success predicate",
                    (one, two, opts) -> one.targetSuccess(two, options));
        }
    },
    /**
     * Add a failure mode to counting
     * The dice roll or pool in counting mode is given a failure predicate, which subtracts 1 if matches. If an element
     * matches both success and failure predicates, they are negated and it counts for 0. The dice roll or pool MUST
     * be in counting mode already by using TARGET_SUCCESS.
     * The right side can be any of the following:
     * 2. >[number] will count any dice greater than or equal to number. This will fail iff any dice face is non-numeric
     * 3. <[number] will count any dice less than or equal to number. This will fail iff any dice face is non-numeric
     * 4. =[number] will count any dice equal to value. This will fail iff any dice face is non-numeric
     * 5. If a pool is provided, the dice will count if any match a value in the pool. This will always work.
     * 6. In any other case, the dice will count if it equals() the term provided. This will always work.
     */
    TARGET_FAILURE("f", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "pool", "failure predicate",
                    (one, two, opts) -> one.targetFailure(two, options));
        }
    }
    ;

    private final String symbol;
    private final int priority;

    private static ValueAndExpression<DiceMathTerm> pull(TermStack<DiceMathTerm> stack, String descriptor) throws ExpressionSyntaxError {
        if (stack.size() == 0) {
            throw new ExpressionSyntaxError("Error pulling " + descriptor + ", no matching term.");
        }
        return stack.pop();
    }

    protected ValueAndExpression<DiceMathTerm> evaluateTwoParameterFunc(TermStack<DiceMathTerm> stack, EvaluatorOptions options, String firstTerm, String secondTerm,
                                                                        TermOperator<DiceMathTerm> operator) {
        var two = DiceOperation.pull(stack, secondTerm);
        var one = DiceOperation.pull(stack, firstTerm);

        var total = operator.compute(one.value(), two.value(), options);
        return new ValueAndExpression<>(total, this, List.of(one, two));
    }

    /**
     * Find the best matching operator for the provided string
     * The operator chosen is whichever one op starts with. In case multiple match,
     * the symbol with longest length is chosen.
     * For example, if op is "ro5", REROLL_ONCE (ro) is returned, even though
     * REROLL (r) matches too.
     * @param op The operator to match against
     * @return The DiceOperation, if one was found
     */
    public static Optional<DiceOperation> findBestPossibleMatch(String op) {
        return Arrays.stream(DiceOperation.values())
                .filter(d -> op.startsWith(d.symbol))
                .max(Comparator.comparing(d -> d.symbol.length()));
    }
}

@FunctionalInterface
interface TermOperator<T> {
    T compute(T first, T other, EvaluatorOptions opts);
}