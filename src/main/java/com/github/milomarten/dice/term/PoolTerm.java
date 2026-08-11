package com.github.milomarten.dice.term;

import com.github.milomarten.dice.die.CountingStrategy;
import com.github.milomarten.dice.die.MarkedRoll;
import com.github.milomarten.dice.die.PoolStrategy;
import com.github.milomarten.dice.die.TotalingStrategy;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.formatting.ExpressionFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public sealed class PoolTerm implements DiceMathTerm permits DieResultTerm {
    public static final Comparator<MarkedRoll<DiceMathTerm>> LOWEST_FIRST =
            Comparator.comparing(mr -> mr.roll.asNumber());
    public static final Comparator<MarkedRoll<DiceMathTerm>> HIGHEST_FIRST =
            Comparator.comparing(mr -> mr.roll.asNumber().negate());

    @Getter protected final List<MarkedRoll<DiceMathTerm>> pool;

    protected boolean canDropOrKeep = true;

    protected TotalingStrategy<DiceMathTerm> totalingStrategy = new PoolStrategy<>();

    public PoolTerm() {
        this.pool = new ArrayList<>();
    }

    public PoolTerm(DiceMathTerm one) {
        this();
        this.pool.add(new MarkedRoll<>(one));
    }

    public PoolTerm(DiceMathTerm one, DiceMathTerm two) {
        this(one);
        this.pool.add(new MarkedRoll<>(two));
    }

    public void addToPool(DiceMathTerm added) {
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

    protected List<MarkedRoll<DiceMathTerm>> copyPool() {
        return pool.stream()
                .map(mr -> new MarkedRoll<>(mr.roll, mr.dropped, mr.exploded))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected PoolTerm create(
            List<MarkedRoll<DiceMathTerm>> pool,
            boolean canDropOrKeep,
            TotalingStrategy<DiceMathTerm> totalingStrategy) {
        return new PoolTerm(pool, canDropOrKeep, totalingStrategy);
    }

    @Override
    public DiceMathTerm drop(boolean lowest, DiceMathTerm quantity, EvaluatorOptions options) {
        if (!canDropOrKeep) {
            throw new ExpressionSyntaxError("Can only drop or keep once");
        }

        var comparator = lowest ? LOWEST_FIRST : HIGHEST_FIRST;

        var newPool = copyPool();
        newPool.stream()
                .sorted(comparator)
                .limit(quantity.asInteger(options))
                .forEach(mr -> mr.dropped = true);

        return create(newPool, false, totalingStrategy);
    }

    @Override
    public DiceMathTerm keep(boolean lowest, DiceMathTerm quantity, EvaluatorOptions options) {
        if (!canDropOrKeep) {
            throw new ExpressionSyntaxError("Can only drop or keep once");
        }
        var comparator = lowest ? HIGHEST_FIRST : LOWEST_FIRST;
        var limit = pool.size() - quantity.asInteger(options);
        var newPool = copyPool();
        if (limit > 0) {
            newPool.stream()
                    .sorted(comparator)
                    .limit(limit)
                    .forEach(mr -> mr.dropped = true);
        }

        return create(newPool, false, totalingStrategy);
    }

    @Override
    public DiceMathTerm targetSuccess(DiceMathTerm predicate, EvaluatorOptions options) {
        if (totalingStrategy instanceof CountingStrategy) {
            throw new ExpressionSyntaxError("Can only target success once");
        }

        var p = parseTermIntoPredicate(predicate, options);

        return create(copyPool(), canDropOrKeep, new CountingStrategy(p));
    }

    @Override
    public DiceMathTerm targetFailure(DiceMathTerm predicate, EvaluatorOptions options) {
        if (totalingStrategy instanceof CountingStrategy cs) {
            if (cs.getFailurePredicate() != null) {
                throw new ExpressionSyntaxError("Can only target failure once");
            }

            var p = parseTermIntoPredicate(predicate, options);

            return create(copyPool(), canDropOrKeep, new CountingStrategy(cs.getSuccessPredicate(), p));
        } else {
            throw new ExpressionSyntaxError("Must target success before failure");
        }
    }

    protected Predicate<MarkedRoll<DiceMathTerm>> parseTermIntoPredicate(DiceMathTerm predicate, EvaluatorOptions options) {
        return switch (predicate) {
            case PredicateTerm pt -> pt.asObjBigDecimalPredicate(mr -> mr.roll.asNumber());
            case PoolTerm pt -> roll -> pt.getPool().stream()
                    .filter(mr -> !mr.dropped)
                    .anyMatch(mr -> Objects.equals(roll.roll, mr.roll));
            default -> roll -> Objects.equals(roll.roll, predicate);
        };
    }

    public String format(ExpressionFormatter<DiceMathTerm> f) {
        return totalingStrategy.formatSummary(f, pool);
    }
}
