package cn.edu.thssdb.predicate.base;

import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.predicate.logical.OrPredicate;
import cn.edu.thssdb.predicate.compare.*;

/**
 * An interface for predicate tree visitor.
 * different visitors can implement different operations on the tree,
 * including: table binding, optimization/simplification, evaluation
 */
public interface PredicateVisitor {

    public void visitLessThanPredicate(LessThanPredicate lessThanPredicate);

    public void visitGreaterThanPredicate(GreaterThanPredicate greaterThanPredicate);

    public void visitGreaterEqualPredicate(GreaterEqualPredicate greaterEqualPredicate);

    public void visitLessEqualPredicate(LessEqualPredicate lessEqualPredicate);

    public void visitEqualPredicate(EqualPredicate equalPredicate);

    public void visitNotEqualPredicate(NotEqualPredicate notEqualPredicate);

    public void visitAndPredicate(AndPredicate andPredicate);

    public void visitOrPredicate(OrPredicate orPredicate);

    public void visitCompareBasePredicate(CompareBasePredicate compareBasePredicate);

    public void visitLogicalBasePredicate(LogicalBasePredicate logicalBasePredicate);
}
