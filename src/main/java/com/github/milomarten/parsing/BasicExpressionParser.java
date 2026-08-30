package com.github.milomarten.parsing;

import com.github.milomarten.evaluator.*;

import java.math.BigDecimal;
import java.text.CharacterIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The most basic of Expression Parsers, which delegate based on if a term is expected or not
 * If a term is expected, the stringToTerm method is called with relevant info. Otherwise, handleNonTerm
 * is provided, for supporting operators and anything else.
 * @param <T> The Term type
 */
public abstract class BasicExpressionParser<T extends Term> implements TermParser<T> {
    @Override
    public void parseNextToken(ShrinkingString iterator, EvaluatorOptions options, ExpressionEvaluator<T> evaluator) {
        if (evaluator.isExpectingTerm()) {
            var term = stringToTerm(iterator, options);
            if (term == null) {
                handleNonTerm(iterator, options, evaluator);
            } else {
                evaluator.push(term);
            }
        } else {
            handleNonTerm(iterator, options, evaluator);
        }
    }

    /**
     * Pull the next term off of the ShrinkingString
     * This method is called if isExpectingTerm is true.
     * If the returned term is non-null, the term is added to the evaluator and parsing proceeds.
     * If null is returned, this indicates that a term was not present, and handleNonTerm is called to parse out
     * something else.
     * @param string The string to pull from
     * @param options The evaluation options
     * @return The term, or null to indicate no term was found.
     */
    protected abstract T stringToTerm(ShrinkingString string, EvaluatorOptions options);

    /**
     * Pull the next non-term off of the ShrinkingString
     * Typically, these are your Operations or BoundedOperations, but is flexible enough to allow anything.
     * The default operation handles both parenthesis and square brackets, throwing an ExpressionSyntaxError if
     * it is not either of those. Subclasses should check their operators first, before finally delegating to
     * the superclass.
     * @param string The string to pull from
     * @param options The evaluation options
     * @param evaluator The evaluator to modify
     */
    protected void handleNonTerm(ShrinkingString string, EvaluatorOptions options, ExpressionEvaluator<T> evaluator) {
        switch (string.currentChar()) {
            case '(': evaluator.pushBoundedOperationStart(new BoundedOperation.Brackets<>('(', ')')); break;
            case '[': evaluator.pushBoundedOperationStart(new BoundedOperation.Brackets<>('[', ']')); break;
            case ')': evaluator.pushBoundedOperationEnd(")"); break;
            case ']': evaluator.pushBoundedOperationEnd("]"); break;
            default: throw new ExpressionSyntaxError("Unable to parse operation out of " + string);
        }
        string.advance();
    }

    /**
     * Pull the next BigDecimal out of a ShrinkingString
     * This method will advance the ShrinkingString until it reaches the largest validly-formatted
     * BigDecimal possible, at which point it will stop.
     * <br>
     * The following rules are enforced:
     * 1. At least one numerical digit (0-9) must be found, or an exception is thrown.
     * 2. A + or - may be the first character ONLY. If a second + or - is found, parsing ends quietly at that point
     * (to best support addition and subtraction)
     * 3. A decimal point may be present anywhere, but only one time. If a second decimal point is encountered,
     * an exception is thrown.
     * 4. If nothing pertinent is found (neither a digit, a +, a -, or a .), an empty optional is returned.
     * Note that e is not supported by this method, even though e is supported in native BigDecimal parsing.
     * For the purposes of rolling dice, supporting e allows nothing but awkwardness...just write out the full decimal!
     * @param string The string to pull from
     * @return The parsed BigDecimal, or empty if nothing related to a number could be found
     */
    protected static Optional<BigDecimal> readNextBigDecimal(ShrinkingString string) {
        var s = new StringBuilder();
        var signPermitted = true;
        var decimalPointPermitted = true;
        while (isNumberFriendlyCharacter(string.currentChar())) {
            char c = string.currentChar();
            if (isSignCharacter(c)) {
                if (signPermitted) {
                    s.append(c);
                    signPermitted = false;
                } else {
                    break; // + or - could be a legitimate operator, assume it is and end it there.
                }
            } else if (c == '.') {
                if (decimalPointPermitted) {
                    s.append(c);
                    decimalPointPermitted = false;
                    signPermitted = false;
                } else {
                    throw new ExpressionSyntaxError("Multiple decimals present");
                }
            } else {
                // physically has to be a number
                s.append(c);
                signPermitted = false;
            }
            string.advance(1);
        }
        if (s.isEmpty()) {
            return Optional.empty();
        }
        var result = s.toString();
        if ("+".equals(result) || "-".equals(result)) {
            // possibly an erroneously-read addition/subtraction sign
            string.advance(-1);
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(result));
    }

    /**
     * Pull the next String out of a ShrinkingString
     * This method will advance the ShrinkingString until it reads a sequence of zero or more characters within
     * two bounding characters. If the escape character is used before a bounding character, the bounding character
     * is considered to be part of the string, and will proceed further until an unescaped bounding character is found.
     * If the escape character is used before another escape character, the escape character is considered to be part
     * of the string.
     * <br>
     * If any other character is after the escape character, an exception is thrown. If the ShrinkingString is
     * fully consumed without reaching an ending boundingChar, an exception is thrown. If the ShrinkingString is
     * not immediately at the boundingChar, an empty Optional is returned.
     * @param string The ShrinkingString to parse
     * @param boundingChar The character to use as both start and end of the string
     * @param escapeChar The character to indicate that the subsequent letter should be read verbatim
     * @return A String, if one could be parsed, or an empty Optional if it could not.
     */
    protected static Optional<String> readNextString(ShrinkingString string, char boundingChar, char escapeChar) {
        if (string.currentChar() != boundingChar) {
            return Optional.empty();
        } else {
            string.advance();
        }

        var s = new StringBuilder();
        while (string.currentChar() != boundingChar && string.currentChar() != CharacterIterator.DONE) {
            var currentChar = string.currentChar();
            if (currentChar == escapeChar) {
                string.advance();
                currentChar = string.currentChar();
                if (currentChar == escapeChar) {
                    s.append(escapeChar);
                    string.advance();
                } else if (currentChar == boundingChar) {
                    s.append(boundingChar);
                    string.advance();
                } else {
                    throw new ExpressionSyntaxError("Unknown escape sequence: " + escapeChar + currentChar);
                }
            } else {
                s.append(currentChar);
                string.advance();
            }
        }

        if (string.currentChar() == boundingChar) {
            string.advance();
            return Optional.of(s.toString());
        } else {
            throw new ExpressionSyntaxError("Unbounded string");
        }
    }

    protected static Optional<String> readToken(ShrinkingString string, Set<Integer> validChars, char startChar) {
        if (string.currentChar() != startChar) {
            return Optional.empty();
        }
        string.advance();

        StringBuilder sb = new StringBuilder();
        while (string.currentChar() != CharacterIterator.DONE && validChars.contains((int) string.currentChar())) {
            sb.append(string.currentChar());
            string.advance();
        }

        if (sb.isEmpty()) {
            throw new ExpressionSyntaxError("Token must be at least one character");
        }
        return Optional.of(sb.toString());
    }


    private static boolean isNumberFriendlyCharacter(char c) {
        return isNumber(c) || c == '.' || isSignCharacter(c);
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isSignCharacter(char c) {
        return c == '+' || c == '-';
    }
}
