package com.github.milomarten.dice.die;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarkedRoll<T> {
    public final T roll;
    public boolean dropped;
    public int exploded;
}
