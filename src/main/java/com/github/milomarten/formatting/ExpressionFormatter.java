package com.github.milomarten.formatting;

import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.Operation;
import com.github.milomarten.evaluator.Term;

import java.util.List;

public interface ExpressionFormatter<T extends Term> {
    String formatTerm(T value);
    String formatOperation(Operation<T> o);
    String formatBoundedOperation(BoundedOperation<T> bo, List<String> contents);
}
