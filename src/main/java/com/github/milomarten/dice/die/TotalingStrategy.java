package com.github.milomarten.dice.die;


import com.github.milomarten.evaluator.Term;
import com.github.milomarten.evaluator.ValueAndExpression;
import com.github.milomarten.formatting.ExpressionFormatter;

import java.math.BigDecimal;
import java.util.List;

public interface TotalingStrategy<T extends Term> {
    BigDecimal totalUp(List<MarkedRoll<ValueAndExpression<T>>> rolls);
    boolean isNumber(List<MarkedRoll<ValueAndExpression<T>>> rolls);
    String formatSummary(ExpressionFormatter<T> formatter, List<MarkedRoll<ValueAndExpression<T>>> rolls);
}
