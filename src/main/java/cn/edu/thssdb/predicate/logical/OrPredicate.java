package cn.edu.thssdb.predicate.logical;

import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;

/**
 * OR
 */
public class OrPredicate extends LogicalBasePredicate {

    public OrPredicate(Predicate lhs, Predicate rhs) {
        super(lhs, rhs);
    }

    @Override
    public void accept(PredicateVisitor predicateVisitor) {
        predicateVisitor.visitOrPredicate(this);
    }
}
