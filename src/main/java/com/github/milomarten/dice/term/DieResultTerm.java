package com.github.milomarten.dice.term;

import com.github.milomarten.dice.die.Die;
import com.github.milomarten.dice.die.MarkedRoll;
import com.github.milomarten.dice.die.SummingStrategy;
import com.github.milomarten.dice.die.TotalingStrategy;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class DieResultTerm extends PoolTerm {
    private final Die<DiceMathTerm> die;
    private final DiceMathTerm numDice;

    private boolean canExplode = true;
    private boolean canReroll = true;

    public DieResultTerm(Die<DiceMathTerm> die, DiceMathTerm numDice, List<DiceMathTerm> rolls) {
        this.die = die;
        this.numDice = numDice;
        this.pool.addAll(rolls.stream()
                .map(MarkedRoll::new)
                .collect(Collectors.toCollection(ArrayList::new)));
        this.totalingStrategy = new SummingStrategy();
    }

    public DieResultTerm(Die<DiceMathTerm> die, DiceMathTerm numDice, List<MarkedRoll<DiceMathTerm>> pool,
                         boolean canDropOrKeep, boolean canExplode, boolean canReroll, TotalingStrategy<DiceMathTerm> totalingStrategy) {
        super(pool);
        this.die = die;
        this.numDice = numDice;
        this.canDropOrKeep = canDropOrKeep;
        this.canExplode = canExplode;
        this.canReroll = canReroll;
        this.totalingStrategy = totalingStrategy;
    }

    @Override
    public boolean isNumber() {
        return totalingStrategy.isNumber(pool);
    }

    @Override
    protected DieResultTerm create(List<MarkedRoll<DiceMathTerm>> pool, boolean canDropOrKeep, TotalingStrategy<DiceMathTerm> totalingStrategy) {
        return new DieResultTerm(die, numDice, pool, canDropOrKeep, canExplode, canReroll, totalingStrategy);
    }

    @Override
    public DiceMathTerm explode(DiceMathTerm predicate, EvaluatorOptions options) {
        if (!canExplode) {
            throw new ExpressionSyntaxError("Can only explode once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        var newTerm = new DieResultTerm(die, numDice, copyPool(), canDropOrKeep, false, canReroll, totalingStrategy);
        newTerm.doRecursiveExplode(1, newTerm.pool, p, options);

        return newTerm;
    }

    private void doRecursiveExplode(int loopNum, List<MarkedRoll<DiceMathTerm>> mostRecentRolls, Predicate<MarkedRoll<DiceMathTerm>> predicate, EvaluatorOptions options) {
        var numToExplode = mostRecentRolls.stream()
                .filter(mr -> {
                    if (!mr.dropped && predicate.test(mr)) {
                        mr.exploded = loopNum;
                        return true;
                    }
                    return false;
                }).count();

        if (numToExplode > 0) {
            var newRolls = die.roll((int) numToExplode, options).stream()
                    .map(MarkedRoll::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            this.pool.addAll(newRolls);

            if (loopNum <= 20) {
                doRecursiveExplode(loopNum + 1, newRolls, predicate, options);
            }
        }
    }

    @Override
    public DiceMathTerm reroll(DiceMathTerm predicate, EvaluatorOptions options) {
        if (!canReroll) {
            throw new ExpressionSyntaxError("Can only explode once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        var newTerm = new DieResultTerm(die, numDice, copyPool(), canDropOrKeep, canExplode, false, totalingStrategy);
        newTerm.doRecursiveReroll(1, newTerm.pool, p, options);

        return newTerm;
    }

    @Override
    public DiceMathTerm rerollOnce(DiceMathTerm predicate, EvaluatorOptions options) {
        if (!canReroll) {
            throw new ExpressionSyntaxError("Can only explode once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        var newTerm = new DieResultTerm(die, numDice, copyPool(), canDropOrKeep, canExplode, false, totalingStrategy);
        newTerm.doRecursiveReroll(100, newTerm.pool, p, options);

        return newTerm;
    }

    private void doRecursiveReroll(int loopNum, List<MarkedRoll<DiceMathTerm>> mostRecentRolls, Predicate<MarkedRoll<DiceMathTerm>> predicate, EvaluatorOptions options) {
        var numToReroll = mostRecentRolls.stream()
                .filter(mr -> {
                    if (!mr.dropped && predicate.test(mr)) {
                        mr.dropped = true;
                        return true;
                    }
                    return false;
                }).count();

        if (numToReroll > 0) {
            var newRolls = die.roll((int) numToReroll, options).stream()
                    .map(MarkedRoll::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            this.pool.addAll(newRolls);

            if (loopNum <= 20) {
                doRecursiveReroll(loopNum + 1, newRolls, predicate, options);
            }
        }
    }

    @Override
    protected Predicate<MarkedRoll<DiceMathTerm>> parseTermIntoPredicate(DiceMathTerm predicate, EvaluatorOptions options) {
        if (predicate instanceof PlaceholderTerm) {
            return roll -> Objects.equals(roll.roll, die.getMaxValue(options));
        }
        return super.parseTermIntoPredicate(predicate, options);
    }

}
