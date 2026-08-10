package com.github.milomarten.dice.term;

import com.github.milomarten.dice.die.*;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class DieResultTerm extends PoolTerm {
//    private static final Comparator<MarkedRoll<ValueAndExpression<DiceMathTerm>>> COMPARING_LOWEST = Comparator.comparing(mr -> mr.roll.value().asNumber());
//    private static final Comparator<MarkedRoll<ValueAndExpression<DiceMathTerm>>> COMPARING_HIGHEST = Comparator.comparing(mr -> mr.roll.value().asNumber().negate());

    private final Die<ValueAndExpression<DiceMathTerm>> die;
    private final ValueAndExpression<DiceMathTerm> numDice;
//    private final List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> rolls;

//    private String operationsString = "";
//    private boolean canDropOrKeep = true;
    private boolean canExplode = true;
    private boolean canReroll = true;

//    private TotalingStrategy<ValueAndExpression<DiceMathTerm>> totalingStrategy = new SummingStrategy();

    public DieResultTerm(Die<ValueAndExpression<DiceMathTerm>> die, ValueAndExpression<DiceMathTerm> numDice, List<ValueAndExpression<DiceMathTerm>> rolls) {
        this.die = die;
        this.numDice = numDice;
        this.pool.addAll(rolls.stream()
                .map(MarkedRoll::new)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public String asString() {
        var poolStr = totalingStrategy.totalUpString(this.pool);
        return "{" + numDice.s() + die.asString() + operationsString + "\uD83E\uDC62" + poolStr + "}";
    }

    @Override
    public boolean isNumber() {
        return totalingStrategy.isNumber(pool);
    }

    @Override
    public DiceMathTerm explode(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        if (!canExplode) {
            throw new ExpressionSyntaxError("Can only explode once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        this.operationsString += "!" + predicate.s();
        doRecursiveExplode(1, pool, p, options);
        this.canExplode = false;
        return this;
    }

    private void doRecursiveExplode(int loopNum, List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> mostRecentRolls, Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> predicate, EvaluatorOptions options) {
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
    public DiceMathTerm reroll(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        if (!canReroll) {
            throw new ExpressionSyntaxError("Can only explode once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        this.operationsString += "r" + predicate.s();
        doRecursiveReroll(1, pool, p, options);
        canReroll = false;

        return this;
    }

    @Override
    public DiceMathTerm rerollOnce(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        if (!canReroll) {
            throw new ExpressionSyntaxError("Can only explode once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        this.operationsString += "r" + predicate.s();
        doRecursiveReroll(100, pool, p, options);
        canReroll = false;

        return this;
    }

    private void doRecursiveReroll(int loopNum, List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> mostRecentRolls, Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> predicate, EvaluatorOptions options) {
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
    protected Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> parseTermIntoPredicate(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        if (predicate.value() instanceof PlaceholderTerm) {
            return roll -> Objects.equals(roll.roll.value(), die.getMaxValue(options).value());
        }
        return super.parseTermIntoPredicate(predicate, options);
    }

}
