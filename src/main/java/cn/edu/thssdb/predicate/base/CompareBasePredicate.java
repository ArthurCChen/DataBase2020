package cn.edu.thssdb.predicate.base;

import cn.edu.thssdb.predicate.Operand;
import cn.edu.thssdb.predicate.base.Predicate;
import com.sun.istack.internal.NotNull;

/**
 * CompareBasePredicate is a base class for logical comparison operations.
 */
public class CompareBasePredicate implements Predicate {

    public Operand lhs;
    public Operand rhs;

    public CompareBasePredicate(@NotNull Operand lhs, @NotNull Operand rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(PredicateVisitor predicateVisitor) {
        predicateVisitor.visitCompareBasePredicate(this);
    }
}
