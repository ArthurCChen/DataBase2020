package cn.edu.thssdb.predicate;

import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;
import cn.edu.thssdb.predicate.compare.*;
import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.predicate.logical.OrPredicate;

/**
 * This visitor modifies the predicate tree to optimize search efficiency
 *
 * Not implemented
 * TODO: optimize
 *
 * Optimize techniques:
 *  1. find comparison between constants
 *  2. optimize the tree structure to minimize the expectation of comparison time
 */
public class OptimizeVisitor implements PredicateVisitor {
    @Override
    public void visitLessThanPredicate(LessThanPredicate lessThanPredicate) {

    }

    @Override
    public void visitGreaterThanPredicate(GreaterThanPredicate greaterThanPredicate) {

    }

    @Override
    public void visitGreaterEqualPredicate(GreaterEqualPredicate greaterEqualPredicate) {

    }

    @Override
    public void visitLessEqualPredicate(LessEqualPredicate lessEqualPredicate) {

    }

    @Override
    public void visitEqualPredicate(EqualPredicate equalPredicate) {

    }

    @Override
    public void visitNotEqualPredicate(NotEqualPredicate notEqualPredicate) {

    }

    @Override
    public void visitAndPredicate(AndPredicate andPredicate) {

    }

    @Override
    public void visitOrPredicate(OrPredicate orPredicate) {

    }

    @Override
    public void visitCompareBasePredicate(CompareBasePredicate compareBasePredicate) {

    }

    @Override
    public void visitLogicalBasePredicate(LogicalBasePredicate logicalBasePredicate) {

    }
}
