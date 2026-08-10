package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SummingStrategy implements TotalingStrategy<ValueAndExpression<DiceMathTerm>> {
    private static final Pattern CROSSOUTS = Pattern.compile("~~");

    @Override
    public BigDecimal totalUp(List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> rolls) {
        return rolls.stream()
                .map(mr -> mr.dropped ? BigDecimal.ZERO : mr.roll.value().asNumber())
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    @Override
    public boolean isNumber(List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> markedRolls) {
        return markedRolls.stream()
                .filter(mr -> !mr.dropped)
                .allMatch(mr -> mr.roll.value().isNumber());
    }
}
