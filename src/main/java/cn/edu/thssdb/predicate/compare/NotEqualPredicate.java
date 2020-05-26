package cn.edu.thssdb.predicate.compare;

import cn.edu.thssdb.predicate.Operand;
import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;

/**
 * !=
 */
public class NotEqualPredicate extends CompareBasePredicate {

    public NotEqualPredicate(Operand lhs, Operand rhs) {
        super(lhs, rhs);
    }

    @Override
    public void accept(PredicateVisitor predicateVisitor) {
        predicateVisitor.visitNotEqualPredicate(this);
    }

    @Override
    public String toString() {
        return String.format("(%s!=%s)", lhs.toString(), rhs.toString());
    }
}
