package com.github.milomarten.dice.term;

import com.github.milomarten.dice.die.CountingStrategy;
import com.github.milomarten.dice.die.MarkedRoll;
import com.github.milomarten.dice.die.PoolStrategy;
import com.github.milomarten.dice.die.TotalingStrategy;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.ValueAndExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public sealed class PoolTerm implements DiceMathTerm permits DieResultTerm {
    public static final Comparator<MarkedRoll<ValueAndExpression<DiceMathTerm>>> LOWEST_FIRST =
            Comparator.comparing(mr -> mr.roll.value().asNumber());
    public static final Comparator<MarkedRoll<ValueAndExpression<DiceMathTerm>>> HIGHEST_FIRST =
            Comparator.comparing(mr -> mr.roll.value().asNumber().negate());

    @Getter protected final List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> pool;

    protected boolean canDropOrKeep = true;

    protected TotalingStrategy<ValueAndExpression<DiceMathTerm>> totalingStrategy = new PoolStrategy<>();

    public PoolTerm() {
        this.pool = new ArrayList<>();
    }

    public PoolTerm(ValueAndExpression<DiceMathTerm> one) {
        this();
        this.pool.add(new MarkedRoll<>(one));
    }

    public PoolTerm(ValueAndExpression<DiceMathTerm> one, ValueAndExpression<DiceMathTerm> two) {
        this(one);
        this.pool.add(new MarkedRoll<>(two));
    }

    public void addToPool(ValueAndExpression<DiceMathTerm> added) {
        this.pool.add(new MarkedRoll<>(added));
    }

    @Override
    public BigDecimal asNumber() {
        return totalingStrategy.totalUp(this.pool);
    }

    @Override
    public boolean isNumber() {
        return pool.stream()
                .filter(mr -> !mr.dropped)
                .count() == 1;
    }

//    public String format(DiceExpressionFormatter formatter) {
//        var p = pool.stream()
//                .map(mr -> {
//                    var base = formatter.formatTerm(mr.roll.value());
//                    if (mr.exploded > 0) {
//                        base = "\uD83D\uDCA5".repeat(mr.exploded) + base;
//                    }
//                    if (mr.dropped) {
//                        base = "~~" + CROSSOUTS.matcher(base).replaceAll("") + "~~";
//                    }
//                    return base;
//                })
//                .collect(Collectors.joining(
//                        ", ", "{", "}"
//                ));
//        if (totalingStrategy.isNumber(pool)) {
//            p += "->" + totalingStrategy.totalUp(pool);
//        }
//        return p;
//    }

    @Override
    public DiceMathTerm drop(boolean lowest, ValueAndExpression<DiceMathTerm> quantity, EvaluatorOptions options) {
        if (!canDropOrKeep) {
            throw new ExpressionSyntaxError("Can only drop or keep once");
        }
        var comparator = lowest ?
                LOWEST_FIRST : HIGHEST_FIRST;
        pool.stream()
                .sorted(comparator)
                .limit(quantity.value().asInteger(options))
                .forEach(mr -> mr.dropped = true);

        this.canDropOrKeep = false;
        return this;
    }

    @Override
    public DiceMathTerm keep(boolean lowest, ValueAndExpression<DiceMathTerm> quantity, EvaluatorOptions options) {
        if (!canDropOrKeep) {
            throw new ExpressionSyntaxError("Can only drop or keep once");
        }
        var comparator = lowest ?
                HIGHEST_FIRST : LOWEST_FIRST;
        var limit = pool.size() - quantity.value().asInteger(options);
        if (limit > 0) {
            pool.stream()
                    .sorted(comparator)
                    .limit(limit)
                    .forEach(mr -> mr.dropped = true);
        }

        this.canDropOrKeep = false;
        return this;
    }

    @Override
    public DiceMathTerm targetSuccess(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        if (totalingStrategy instanceof CountingStrategy) {
            throw new ExpressionSyntaxError("Can only target success once");
        }

        Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> p = parseTermIntoPredicate(predicate, options);

        this.totalingStrategy = new CountingStrategy(p);

        return this;
    }

    @Override
    public DiceMathTerm targetFailure(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        if (totalingStrategy instanceof CountingStrategy cs) {
            if (cs.getFailurePredicate() != null) {
                throw new ExpressionSyntaxError("Can only target failure once");
            }

            Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> p = parseTermIntoPredicate(predicate, options);

            cs.setFailurePredicate(p);

            return this;

        } else {
            throw new ExpressionSyntaxError("Must target success before failure");
        }
    }

    protected Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> parseTermIntoPredicate(ValueAndExpression<DiceMathTerm> predicate, EvaluatorOptions options) {
        return switch (predicate.value()) {
            case PredicateTerm pt -> pt.asObjBigDecimalPredicate(mr -> mr.roll.value().asNumber());
            case PoolTerm pt -> roll -> pt.getPool().stream()
                    .filter(mr -> !mr.dropped)
                    .anyMatch(mr -> Objects.equals(roll.roll.value(), mr.roll.value()));
            default -> roll -> Objects.equals(roll.roll.value(), predicate.value());
        };
    }
}
