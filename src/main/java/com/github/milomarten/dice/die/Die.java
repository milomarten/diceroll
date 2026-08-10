package com.github.milomarten.dice.die;

import com.github.milomarten.evaluator.EvaluatorOptions;

import java.util.List;

public interface Die<T> {
    List<T> roll(int qty, EvaluatorOptions options);
    T getMaxValue(EvaluatorOptions options);
}
