package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
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

/**
 * A strategy to count the number of rolls matching a predicate.
 * Of the non-dropped rolls, the result of totalUp is the number
 * of rolls that match the success predicate, minus the number
 * of rolls that match the failure predicate (if present).
 * Once the TARGET_SUCCESS dice operation is used, the strategy is changed to
 * this one.
 * totalUp will always return a number, since the predicate does not depend
 * on term type.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class CountingStrategy implements TotalingStrategy<DiceMathTerm> {
    private static final Pattern CROSSOUTS = Pattern.compile("~~");

    private final Predicate<MarkedRoll<DiceMathTerm>> successPredicate;
    @Setter private Predicate<MarkedRoll<DiceMathTerm>> failurePredicate;

    @Override
    public BigDecimal totalUp(List<MarkedRoll<DiceMathTerm>> rolls) {
        return rolls.stream()
                .filter(mr -> !mr.dropped)
                .map(mr -> {
                    var counter = BigDecimal.ZERO;
                    if (successPredicate.test(mr)) {
                        counter = counter.add(BigDecimal.ONE);
                    }
                    if (failurePredicate != null && failurePredicate.test(mr)) {
                        counter = counter.subtract(BigDecimal.ONE);
                    }
                    return counter;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    @Override
    public boolean isNumber(List<MarkedRoll<DiceMathTerm>> markedRolls) {
        return true; // counting will *always* turn into a number
    }

    @Override
    public String formatSummary(ExpressionFormatter<DiceMathTerm> formatter, List<MarkedRoll<DiceMathTerm>> rolls) {
        var pool = rolls.stream()
                .map(mr -> {
                    var str = formatter.formatTerm(mr.roll);
                    if (mr.exploded > 0) {
                        str = "\uD83D\uDCA5".repeat(mr.exploded) + str;
                    }
                    if (mr.dropped) {
                        str = "~~" + CROSSOUTS.matcher(str).replaceAll("") + "~~";
                    } else {
                        if (successPredicate.test(mr)) {
                            str = "✔" + str;
                        }
                        if (failurePredicate != null && failurePredicate.test(mr)) {
                            str = "X" + str;
                        }
                    }
                    return str;
                })
                .collect(Collectors.joining(",", "{", "}"));
        return pool + "➡" + totalUp(rolls);
    }
}
