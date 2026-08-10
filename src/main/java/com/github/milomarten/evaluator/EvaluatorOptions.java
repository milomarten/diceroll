package com.github.milomarten.evaluator;

import lombok.Builder;
import lombok.Data;

import java.math.RoundingMode;

@Builder
@Data
public class EvaluatorOptions {
    @Builder.Default private int maximumNumberOfTerms = Integer.MAX_VALUE;
    @Builder.Default private RoundingMode roundingMode = RoundingMode.DOWN;

    public boolean hasTermMaximum() {
        return maximumNumberOfTerms != Integer.MAX_VALUE;
    }
}
