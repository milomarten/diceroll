package com.github.milomarten.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * A fixed-size table of entries, which may vary in likelihood
 * Each table has an integer weight, which confers its likelihood relative
 * to all other entries. The odds of selection are (weight / sum of all weights).
 * @param <T> The contents of the table
 */
public class WeightedTable<T> implements RandomlySelected<T> {
    private int totalWeightSize = 0;

    private final TreeMap<Integer, T> table = new TreeMap<>();

    /**
     * Add an entry to the table
     * Null is not permitted, and only positive weights ar permitted
     * @param weight The weight of the entry
     * @param entry The entry to add
     * @return This table, for chaining
     */
    public WeightedTable<T> addEntry(int weight, T entry) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight is nonpositive");
        }
        table.put(totalWeightSize, Objects.requireNonNull(entry));
        totalWeightSize += weight;
        return this;
    }

    @Override
    public T get(UniformRandomProvider randomness) {
        var roll = randomness.nextInt(totalWeightSize);
        return table.floorEntry(roll).getValue();
    }

    @Override
    public Stream<T> getInfinite(UniformRandomProvider randomness) {
        return randomness.ints(0, totalWeightSize)
                .mapToObj(i -> table.floorEntry(i).getValue());
    }
}
