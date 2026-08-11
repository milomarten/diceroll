package com.github.milomarten.dice.die;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * A roll that can be marked with certain statuses
 * @param <T> The term in the roll
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class MarkedRoll<T> {
    /**
     * The face value of the roll itself
     */
    public final T roll;
    /**
     * Whether this roll has been dropped or not
     */
    public boolean dropped;
    /**
     * Whether this roll has exploded or not.
     * The number represents the "level" of explosion. On the first check, any
     * exploding dice will have exploded = 1. If any dice resulting from that round of explosions
     * *also* explode, exploded = 2, and so on.
     */
    public int exploded;
}
