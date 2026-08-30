package com.github.milomarten.evaluator;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.Function;

@Builder
@Data
public class EvaluatorOptions {
    @Builder.Default private int maximumNumberOfTerms = Integer.MAX_VALUE;
    @Builder.Default private RoundingMode roundingMode = RoundingMode.DOWN;
    @Builder.Default private int maximumExplodes = 20;
    @Builder.Default private int maximumRerolls = 20;
    @Builder.Default private UniformRandomProvider randomSource = RandomSource.MT.create();
    @Builder.Default private Function<String, Optional<Term>> tokenResolver
            = s -> Optional.empty();

    public boolean hasTermMaximum() {
        return maximumNumberOfTerms != Integer.MAX_VALUE;
    }
}
