package cn.edu.thssdb.predicate.logical;

import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;

/**
 * AND
 */
public class AndPredicate extends LogicalBasePredicate {

    public AndPredicate(Predicate lhs, Predicate rhs) {
        super(lhs, rhs);
    }

    @Override
    public void accept(PredicateVisitor predicateVisitor) {
        predicateVisitor.visitAndPredicate(this);
    }

    @Override
    public String toString() {
        return String.format("(%s and %s)", lhs.toString(), rhs.toString());
    }
}
