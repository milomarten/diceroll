package com.github.milomarten.formatting;

import com.github.milomarten.evaluator.BoundedOperation;
import com.github.milomarten.evaluator.Operation;
import com.github.milomarten.evaluator.Term;
import com.github.milomarten.evaluator.ValueAndExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * A Node class used for facilitating the LineByLineFormatter.
 * This node contains two operations which, in tandem, give an output for each step of an Evaluation.
 * 1. format() computes the string field for every node. The string of a leaf node is simply its value, while branches
 * are formatted as a combination of the operation and its children's string field. Thus, each node's string is
 * based on its children's string.
 * 2. pullUp() trims all the leaves, potentially leaving new leaves in their stead.
 * Each successive call to pullUp() simplifies the tree, and the result of format() goes from showing the operation
 * (when it's a branch) to the result of the operation (when it's a leaf). When the root has no children, only the final
 * answer remains.
 * @param <T> The term type
 */
@RequiredArgsConstructor
@ToString
class UnNode<T extends Term> {
    @ToString.Exclude private final ValueAndExpression<T> value;
    @Getter @Setter private String string = "";
    @Getter private final List<UnNode<T>> children = new ArrayList<>();

    /**
     * Format this node's children, and itself
     * For leaf UnNodes (nodes with no children), this.string becomes the
     * result of calling the formatter's formatTerm() method. For non-leafs
     * with an Operation, this.string becomes the result of calling either formatOperation
     * or formatBoundedOperation with the children's strings.
     * formatBoundedOperation() method, with the children nodes all passed in as one list.
     * @param formatter The formatter to use.
     */
    public void format(ExpressionFormatter<T> formatter) {
        if (isLeaf()) {
            string = formatter.formatTerm(value.value());
        } else {
            children.forEach(n -> n.format(formatter));
            if (value.operation() instanceof Operation<?> o) {
                string = formatter.formatOperation((Operation<T>) o, children.stream().map(UnNode::getString).toList());
            } else if (value.operation() instanceof BoundedOperation<?> bo) {
                string = formatter.formatBoundedOperation((BoundedOperation<T>) bo, children.stream().map(UnNode::getString).toList());
            }
        }
    }

    /**
     * Shrink this tree by one level.
     * Any node only containing leaves as children have the children removed.
     */
    public void pullUp() {
        if (children.stream().allMatch(UnNode::isLeaf)) {
            children.clear();
        } else {
            children.forEach(UnNode::pullUp);
        }
    }

    /**
     * Check if this node is a leaf
     * @return True, if this node has no children
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }
}
