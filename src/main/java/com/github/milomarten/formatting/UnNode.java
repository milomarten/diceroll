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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ToString
public class UnNode<T extends Term> {
    private final int level;
    @ToString.Exclude private final UnNode<T> parent;
    @ToString.Exclude private final ValueAndExpression<T> value;
    @Getter @Setter private String string = "";
    @Getter private final List<UnNode<T>> children = new ArrayList<>();

    public void format(ExpressionFormatter<T> formatter) {
        if (isLeaf()) {
            string = formatter.formatTerm(value.value());
        } else {
            children.forEach(n -> n.format(formatter));
            if (value.operation() instanceof Operation<?> o) {
                string = children.stream()
                        .map(n -> n.string)
                        .collect(Collectors.joining(formatter.formatOperation((Operation<T>) o), "(", ")"));
            } else if (value.operation() instanceof BoundedOperation<?> bo) {
                string = formatter.formatBoundedOperation((BoundedOperation<T>) bo, children.stream().map(UnNode::getString).toList());
            }
        }
    }

    public void pullUp() {
        if (children.stream().allMatch(UnNode::isLeaf)) {
            children.clear();
        } else {
            children.forEach(UnNode::pullUp);
        }
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}
