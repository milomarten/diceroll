package com.github.milomarten.dice.die;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class MarkedRoll<T> {
    public final T roll;
    public boolean dropped;
    public int exploded;
}
