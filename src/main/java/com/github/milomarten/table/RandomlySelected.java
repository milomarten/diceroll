package com.github.milomarten.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A source of randomly-selectable items
 * @param <T> The items returned
 */
public interface RandomlySelected<T> {
    /**
     * Get a randomly selected item
     * @param randomness The source of randomness
     * @return A randomly-selected item
     */
    T get(UniformRandomProvider randomness);

    /**
     * Get an infinite Stream of randomly-selected items
     * @param randomness The source of randomness
     * @return A randomly-selected item
     */
    Stream<T> getInfinite(UniformRandomProvider randomness);

    /**
     * Get a list of randomly-selected items
     * Quantity must be non-negative
     * @param qty The number to get
     * @param randomness The source of randomness
     * @return A list of randomly-selected items of length qty
     */
    default List<T> get(int qty, UniformRandomProvider randomness) {
        if (qty < 0) {
            throw new IllegalArgumentException("Quantity is negative");
        } else if (qty == 0) {
            return List.of();
        }
        return getInfinite(randomness)
                .limit(qty)
                .toList();
    }

    /**
     * Map this stream into another data type
     * Whenever a get* method is invoked on the returned map, the source is queried, and
     * the mapper transforms it. An exception is thrown if the mapper returns null
     * @param mapper The mapper to transform
     * @return A RandomlySelected that maps the output of this one
     * @param <U> The new type
     */
    default <U> RandomlySelected<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper);
        var self = this;
        return new RandomlySelected<>() {
            @Override
            public U get(UniformRandomProvider randomness) {
                return Objects.requireNonNull(mapper.apply(self.get(randomness)));
            }

            @Override
            public Stream<U> getInfinite(UniformRandomProvider randomness) {
                return self.getInfinite(randomness)
                        .map(entry -> Objects.requireNonNull(mapper.apply(entry)));
            }
        };
    }
}
