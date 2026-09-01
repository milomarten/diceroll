package com.github.milomarten.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.*;
import java.util.stream.Stream;

/**
 * A fixed-size table of entries, each of which is equally likely
 * @param <T> The contents of the table
 */
public class UnweightedTable<T> implements RandomlySelected<T> {
    private final List<T> entries = new ArrayList<>();

    /**
     * Add an entry to the table
     * Null is not permitted
     * @param entry The entry
     * @return This table, for chaining
     */
    public UnweightedTable<T> addEntry(T entry) {
        this.entries.add(Objects.requireNonNull(entry));
        return this;
    }

    @Override
    public T get(UniformRandomProvider randomness) {
        return entries.get(randomness.nextInt(0, entries.size()));
    }

    @Override
    public Stream<T> getInfinite(UniformRandomProvider randomness) {
        return randomness.ints(0, entries.size())
                .mapToObj(entries::get);
    }
}
