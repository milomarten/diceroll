package com.github.milomarten.parsing;

import com.github.milomarten.evaluator.ExpressionSyntaxError;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BasicExpressionParserTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "50",
            "3.4",
            "+5",
            "-5",
            "0.4",
            ".4",
            "-.4"
    })
    public void testJustNumbers(String expressions) {
        var bd = BasicExpressionParser.readNextBigDecimal(new ShrinkingString(expressions));
        assertEquals(new BigDecimal(expressions), bd);
    }

    @ParameterizedTest
    @CsvSource({
            "50+20,50,+20",
            "-10*3,-10,*3",
            "1.0+4,1.0,+4",
            ".5*3,0.5,*3",
            "-.5+.5,-0.5,+.5"
    })
    public void testNumbersInExpression(String expression, String result, String excess) {
        var shrinking = new ShrinkingString(expression);
        var bd = BasicExpressionParser.readNextBigDecimal(shrinking);
        assertEquals(new BigDecimal(result), bd);
        assertEquals(excess, shrinking.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        ".", "++5", "1.2.3", "flop"
    })
    public void testBadNumbers(String expression) {
        assertThrows(ExpressionSyntaxError.class, () -> {
            BasicExpressionParser.readNextBigDecimal(new ShrinkingString(expression));
        });
    }
}