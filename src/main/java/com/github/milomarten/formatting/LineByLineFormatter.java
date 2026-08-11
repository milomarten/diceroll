package com.github.milomarten.formatting;

import com.github.milomarten.evaluator.Term;
import com.github.milomarten.evaluator.ValueAndExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Format the result of a parsing as multiple lines of work.
 * Each line will break down the expression tree one level at a time until the computation is complete. This is
 * done by repeatedly formatting the deepest layers on the expression tree until complete. As an annotated example:
 * Assume the input expression was (2d6+2)*(2d10+1):
 * The first step is to encapsulate each term into parenthesis according to order of operations: (((2d6)+2)*((d10)+1))
 * The deepest layer are the two dice rolls, so they are rolled. The next lines are: (({2,2=>4}+2)*({7}+1))
 * The addition steps are now the deepest layer, so they are evaluated. The line becomes: (6*8)
 * There is only one possibility now, so it is evaluated. The line becomes: 48, the answer.
 * <br>
 * Note that the input tree is unmodified, and no computation actually occurs at this point, since the input tree
 * already has each computation step embedded in itself.
 */
public class LineByLineFormatter {
    /**
     * Format the result of an expression using the provided formatter.
     * @param tree The tree to be output
     * @param formatter The formatter to use when creating the output
     * @return A list of the result of each step in the evaluation
     * @param <T> The term type
     */
    public static <T extends Term> List<String> format(ValueAndExpression<T> tree, ExpressionFormatter<T> formatter) {
        var output = new ArrayList<String>();

        var node = copyToUnNode(tree);

        node.format(formatter);
        output.add(node.getString());
        while (!node.isLeaf()) {
            node.pullUp();
            node.format(formatter);
            output.add(node.getString());
        }

        return output;
    }

    private static <T extends Term> UnNode<T> copyToUnNode(ValueAndExpression<T> tree) {
        var unnode = new UnNode<>(tree);
        for (var child : tree.children()) {
            unnode.getChildren().add(copyToUnNode(child));
        }
        return unnode;
    }

}
