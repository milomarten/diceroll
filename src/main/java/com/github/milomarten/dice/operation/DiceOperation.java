package com.github.milomarten.dice.operation;

import com.github.milomarten.dice.die.Die;
import com.github.milomarten.dice.die.DiscreteDie;
import com.github.milomarten.dice.die.NDie;
import com.github.milomarten.dice.die.PoolDie;
import com.github.milomarten.dice.term.*;
import com.github.milomarten.evaluator.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;

@RequiredArgsConstructor
@Getter
public enum DiceOperation implements Operation<DiceMathTerm> {
    /**
     * Add two terms.
     */
    ADD("+", 10){
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "addend", "addend", DiceMathTerm::add);
        }
    },
    /**
     * Subtract two terms.
     */
    SUBTRACT("-", 10) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "minuend", "subtrahend", DiceMathTerm::subtract);
        }
    },
    /**
     * Multiply two terms.
     */
    MULTIPLY("*", 8) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterFunc(termStack, options, "multiplicand", "multiplier", DiceMathTerm::multiply);
        }
    },
    /**
     * Divide two terms.
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
    COMMA(",", 20) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            var newEntry = DiceOperation.pull(termStack, "pool entry");
            var numberOrPool = DiceOperation.pull(termStack, "num of dice");

            if (numberOrPool.value() instanceof PoolTerm pt) {
                pt.addToPool(newEntry);
                var newChildren = new ArrayList<>(numberOrPool.children());
                newChildren.add(newEntry);
                return new ValueAndExpression<>(pt, pt.asString(), this, newChildren);
            } else {
                var newPool = new PoolTerm(numberOrPool, newEntry);
                return new ValueAndExpression<>(newPool, newPool.asString(), this, List.of(numberOrPool, newEntry));
            }
        }
    },
    // Dice Shit
    DICE("d", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            var numSides = DiceOperation.pull(termStack, "number of sides");
            var numDice = DiceOperation.pull(termStack, "num of dice");

            var numDiceInt = numDice.value().asInteger(options);

            Die<ValueAndExpression<DiceMathTerm>> die;
            if (numSides.value() instanceof PoolTerm pool) {
                die = new PoolDie(pool);
            } else if (numSides.value() instanceof CoinFlipTerm) {
                die = new DiscreteDie('C', CoinFlipTerm.HEADS, CoinFlipTerm.TAILS);
            } else {
                die = new NDie(numSides);
            }
            var resultant = new DieResultTerm(die, numDice, die.roll(numDiceInt, options));

            return new ValueAndExpression<>(resultant, resultant.asString(), this, List.of(numDice, numSides));
        }

        @Override
        public DiceMathTerm getImplicitLeftTerm() {
            return ImplicitNumberTerm.ONE;
        }
    },
    DROP_LOWEST("dl", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "pool", "num to drop",
                    (one, two, opts) -> one.drop(true, two, options));
        }

        @Override
        public DiceMathTerm getImplicitRightTerm() {
            return ImplicitNumberTerm.ONE;
        }
    },
    DROP_HIGHEST("dh", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "pool", "num to drop",
                    (one, two, opts) -> one.drop(false, two, options));
        }

        @Override
        public DiceMathTerm getImplicitRightTerm() {
            return ImplicitNumberTerm.ONE;
        }
    },
    KEEP_LOWEST("kl", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "pool", "num to keep",
                    (one, two, opts) -> one.keep(true, two, options));
        }

        @Override
        public DiceMathTerm getImplicitRightTerm() {
            return ImplicitNumberTerm.ONE;
        }
    },
    KEEP_HIGHEST("kh", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "pool", "num to keep",
                    (one, two, opts) -> one.keep(false, two, options));
        }

        @Override
        public DiceMathTerm getImplicitRightTerm() {
            return ImplicitNumberTerm.ONE;
        }
    },
    EXPLODE("!", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "dice pool", "explosion predicate",
                    (one, two, opts) -> one.explode(two, options));
        }

        @Override
        public DiceMathTerm getImplicitRightTerm() {
            return PlaceholderTerm.INSTANCE;
        }
    },
    REROLL("r", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "dice pool", "reroll predicate",
                    (one, two, opts) -> one.reroll(two, options));
        }
    },
    REROLL_ONCE("ro", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "dice pool", "reroll predicate",
                    (one, two, opts) -> one.rerollOnce(two, options));
        }
    },
    TARGET_SUCCESS("s", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "pool", "success predicate",
                    (one, two, opts) -> one.targetSuccess(two, options));
        }
    },
    TARGET_FAILURE("f", 4) {
        @Override
        public ValueAndExpression<DiceMathTerm> evaluate(TermStack<DiceMathTerm> termStack, EvaluatorOptions options) {
            return evaluateTwoParameterDiceFunc(termStack, options, "pool", "failure predicate",
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
        var asString = "(" + one.s() + " " + symbol + " " + two.s() + ")";

        return new ValueAndExpression<>(total, asString, this, List.of(one, two));
    }

    protected ValueAndExpression<DiceMathTerm> evaluateTwoParameterDiceFunc(TermStack<DiceMathTerm> stack, EvaluatorOptions options, String firstTerm, String secondTerm,
                                                                            DiceTermOperator<DiceMathTerm> operator) {
        var two = DiceOperation.pull(stack, secondTerm);
        var one = DiceOperation.pull(stack, firstTerm);
        var total = operator.compute(one.value(), two, options);

        return new ValueAndExpression<>(total, total.asString(), this, List.of(one, two));
    }

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

@FunctionalInterface
interface DiceTermOperator<T extends Term> {
    T compute(T first, ValueAndExpression<T> other, EvaluatorOptions opts);
}