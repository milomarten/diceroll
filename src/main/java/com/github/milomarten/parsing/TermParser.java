package com.github.milomarten.parsing;

import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionEvaluator;
import com.github.milomarten.evaluator.Term;

public interface TermParser<T extends Term> {
    void parseNextToken(ShrinkingString iterator, EvaluatorOptions options, ExpressionEvaluator<T> evaluator);
}
