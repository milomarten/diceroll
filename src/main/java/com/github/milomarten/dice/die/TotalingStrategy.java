package com.github.milomarten.dice.die;

import java.math.BigDecimal;
import java.util.List;

public interface TotalingStrategy<T> {
    BigDecimal totalUp(List<MarkedRoll<T>> rolls);
    String totalUpString(List<MarkedRoll<T>> rolls);
    boolean isNumber(List<MarkedRoll<T>> rolls);
}
