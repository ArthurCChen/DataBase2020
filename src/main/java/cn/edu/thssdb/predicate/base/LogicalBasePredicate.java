package cn.edu.thssdb.predicate.base;

import com.sun.istack.internal.NotNull;

/**
 * This is a base class for all logical operators,
 * including == and !=
 */
public class LogicalBasePredicate implements Predicate {

    public Predicate lhs;
    public Predicate rhs;

    public LogicalBasePredicate(@NotNull Predicate lhs, @NotNull Predicate rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(PredicateVisitor predicateVisitor) {
        predicateVisitor.visitLogicalBasePredicate(this);
    }
}
