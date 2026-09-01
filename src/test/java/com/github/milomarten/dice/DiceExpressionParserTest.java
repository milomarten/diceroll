package com.github.milomarten.dice;

import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.dice.term.NumberTerm;
import com.github.milomarten.dice.term.StringTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.formatting.LineByLineFormatter;
import com.github.milomarten.parsing.StringExpressionEvaluator;
import com.github.milomarten.table.UnweightedTable;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Test;

class DiceExpressionParserTest {
    public static final StringExpressionEvaluator<DiceMathTerm> EVAL =
            new StringExpressionEvaluator<>(new DiceExpressionParser());

    @Test
    public void test() {
        var expr = "8 + 4 ";
        var result = EVAL.evaluate(expr);
        var formatted = LineByLineFormatter.format(result, new DiceResultFormatter());

        System.out.println(expr);
        formatted.forEach(line -> System.out.println("- " + line));
    }

    @Test
    public void testTableTokens() {
        var table = new UnweightedTable<DiceMathTerm>()
                .addEntry(new NumberTerm(1))
                .addEntry(new NumberTerm(2))
                .addEntry(new NumberTerm(3))
                .addEntry(new NumberTerm(4))
                .addEntry(new NumberTerm(5));

        var tokens = new TokenTable();
        tokens.addRandomlySelected("test", table, RandomSource.MT.create());

        var ctx = EvaluatorOptions.builder()
                .tokenResolver(tokens)
                .build();

        var expr = "8 + 2d@test";
        var result = EVAL.evaluate(expr, ctx);
        var formatted = LineByLineFormatter.format(result, new DiceResultFormatter());

        System.out.println(expr);
        formatted.forEach(line -> System.out.println("- " + line));
    }

    @Test
    public void testTableTokensString() {
        var table = new UnweightedTable<DiceMathTerm>()
                .addEntry(new StringTerm("BULBASAUR"))
                .addEntry(new StringTerm("CHARMANDER"))
                .addEntry(new StringTerm("SQUIRTLE"));

        var tokens = new TokenTable();
        tokens.addRandomlySelected("starters", table, RandomSource.MT.create());

        var ctx = EvaluatorOptions.builder()
                .tokenResolver(tokens)
                .build();

        var expr = "2d@starters";
        var result = EVAL.evaluate(expr, ctx);
        var formatted = LineByLineFormatter.format(result, new DiceResultFormatter());

        System.out.println(expr);
        formatted.forEach(line -> System.out.println("- " + line));
    }
}