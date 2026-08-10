package com.github.milomarten.parsing;

import lombok.RequiredArgsConstructor;

import java.text.CharacterIterator;

/**
 * A semi-iterator through a String
 * In essence, this is a stateful substring. Uses of this class will "pull off" one or more characters from the front of the
 * substring, leaving the rest for further processing.
 * This differs from the CharacterIterator in that you can still read the entire remaining String, and you can advance multiple characters
 * in one shot instead of read one at a time.
 */
@RequiredArgsConstructor
public class ShrinkingString {
    private final String string;
    private int index = 0;

    /**
     * Get the char currently at the front of the string.
     * If the string is empty, character \uFFFF is returned.
     * @return The character at the front of the string
     */
    public char currentChar() {
        if (isEmpty()) {
            return CharacterIterator.DONE;
        } else {
            return string.charAt(index);
        }
    }

    /**
     * Advance one character forward on the string.
     */
    public void advance() {
        advance(1);
    }

    /**
     * Advance some quantity of characters forward on the string.
     * @param qty The number of steps to take from the current index.
     */
    public void advance(int qty) {
        this.index += qty;
    }

    /**
     * Check if this string is empty
     * @return True if there is no more characters available to consume
     */
    public boolean isEmpty() {
        return this.index >= string.length();
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : string.substring(this.index);
    }
}
