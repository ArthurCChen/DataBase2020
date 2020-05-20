package cn.edu.thssdb.predicate.compare;

import cn.edu.thssdb.predicate.Operand;
import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;

/**
 * >
 */
public class GreaterThanPredicate extends CompareBasePredicate {

    public GreaterThanPredicate(Operand lhs, Operand rhs) {
        super(lhs, rhs);
    }

    @Override
    public void accept(PredicateVisitor predicateVisitor) {
        predicateVisitor.visitGreaterThanPredicate(this);
    }
}
