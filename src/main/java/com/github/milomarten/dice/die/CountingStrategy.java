package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.ValueAndExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CountingStrategy implements TotalingStrategy<ValueAndExpression<DiceMathTerm>> {
    private final Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> successPredicate;
    @Getter @Setter private Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> failurePredicate;

    @Override
    public BigDecimal totalUp(List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> rolls) {
        return rolls.stream()
                .filter(mr -> !mr.dropped)
                .map(mr -> {
                    if (successPredicate.test(mr)) {
                        return BigDecimal.ONE;
                    } else if (failurePredicate != null && failurePredicate.test(mr)) {
                        return BigDecimal.ONE.negate();
                    } else {
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    @Override
    public boolean isNumber(List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> markedRolls) {
        return true; // counting will *always* turn into a number
    }
}
