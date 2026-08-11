package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.ValueAndExpression;
import com.github.milomarten.formatting.ExpressionFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class CountingStrategy implements TotalingStrategy<DiceMathTerm> {
    private static final Pattern CROSSOUTS = Pattern.compile("~~");

    private final Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> successPredicate;
    @Setter private Predicate<MarkedRoll<ValueAndExpression<DiceMathTerm>>> failurePredicate;

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

    @Override
    public String formatSummary(ExpressionFormatter<DiceMathTerm> formatter, List<MarkedRoll<ValueAndExpression<DiceMathTerm>>> rolls) {
        var pool = rolls.stream()
                .map(mr -> {
                    var str = formatter.formatTerm(mr.roll);
                    if (mr.exploded > 0) {
                        str = "\uD83D\uDCA5".repeat(mr.exploded) + str;
                    }
                    if (mr.dropped) {
                        str = "~~" + CROSSOUTS.matcher(str).replaceAll("") + "~~";
                    } else if (successPredicate.test(mr)) {
                        str = "✔" + str;
                    } else if (failurePredicate != null && failurePredicate.test(mr)) {
                        str = "❌" + str;
                    }
                    return str;
                })
                .collect(Collectors.joining(",", "{", "}"));
        return pool + "➡" + totalUp(rolls);
    }
}
