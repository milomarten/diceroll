package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.evaluator.ValueAndExpression;
import com.github.milomarten.formatting.ExpressionFormatter;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PoolStrategy<T extends DiceMathTerm> implements TotalingStrategy<T> {
    private static final Pattern CROSSOUTS = Pattern.compile("~~");

    @Override
    public BigDecimal totalUp(List<MarkedRoll<T>> markedRolls) {
        var validRolls = markedRolls
                .stream()
                .filter(mr -> !mr.dropped)
                .toList();
        if (validRolls.size() == 1) {
            return validRolls.getFirst().roll.asNumber();
        } else {
            throw new ExpressionSyntaxError("Tried to unwrap a pool as a number");
        }
    }

    @Override
    public boolean isNumber(List<MarkedRoll<T>> markedRolls) {
        return markedRolls.stream()
                .filter(mr -> !mr.dropped)
                .count() == 1;
    }

    @Override
    public String formatSummary(ExpressionFormatter<T> formatter, List<MarkedRoll<T>> rolls) {
        return rolls.stream()
                .map(mr -> {
                    var str = formatter.formatTerm(mr.roll);
                    if (mr.exploded > 0) {
                        str = "\uD83D\uDCA5".repeat(mr.exploded) + str;
                    }
                    if (mr.dropped) {
                        str = "~~" + CROSSOUTS.matcher(str).replaceAll("") + "~~";
                    }
                    return str;
                })
                .collect(Collectors.joining(",", "{", "}"));
    }
}
