package com.github.milomarten.dice.die;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.ValueAndExpression;
import com.github.milomarten.formatting.ExpressionFormatter;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SummingStrategy implements TotalingStrategy<DiceMathTerm> {
    private static final Pattern CROSSOUTS = Pattern.compile("~~");

    @Override
    public BigDecimal totalUp(List<MarkedRoll<DiceMathTerm>> rolls) {
        return rolls.stream()
                .map(mr -> mr.dropped ? BigDecimal.ZERO : mr.roll.asNumber())
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    @Override
    public boolean isNumber(List<MarkedRoll<DiceMathTerm>> markedRolls) {
        return markedRolls.stream()
                .filter(mr -> !mr.dropped)
                .allMatch(mr -> mr.roll.isNumber());
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
                    }
                    return str;
                })
                .collect(Collectors.joining(",", "{", ""));
        if (isNumber(rolls) && rolls.size() > 1) {
            pool += "=>" + totalUp(rolls) + "}";
        } else {
            pool += "}";
        }
        return pool;
    }
}
