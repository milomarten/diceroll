package com.github.milomarten.table;

import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A cross-product of two RandomlySelected sources
 * When generating entries, both the left and right RandomlySelected are queried
 * and combined.
 * @param <T> The output of the left RandomlySelected
 * @param <U> The output of the right RandomlySelected
 * @param <M> The result of merging the two sources
 */
@RequiredArgsConstructor
public class MergedRandomlySelected<T, U, M> implements RandomlySelected<M> {
    private final RandomlySelected<T> left;
    private final RandomlySelected<U> right;
    private final BiFunction<T, U, M> combiner;

    @Override
    public M get(UniformRandomProvider randomness) {
        return combiner.apply(
                left.get(randomness),
                right.get(randomness)
        );
    }

    @Override
    public Stream<M> getInfinite(UniformRandomProvider randomness) {
        return Stream.generate(() -> get(randomness));
    }
}
