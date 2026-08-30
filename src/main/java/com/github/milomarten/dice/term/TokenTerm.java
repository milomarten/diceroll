package com.github.milomarten.dice.term;

import lombok.experimental.Delegate;

public record TokenTerm(String name, @Delegate DiceMathTerm wrapped) implements DiceMathTerm {
}