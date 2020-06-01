package cn.edu.thssdb.predicate;

import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;
import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.predicate.logical.OrPredicate;
import cn.edu.thssdb.predicate.compare.*;
import cn.edu.thssdb.schema.Entry;

/**
 * This is a predicate evaluator.
 * To use, first bind the visitor to a row, visit the root node, then get the answer.
 * Before evaluation, also make sure the predicate tree has been successfully bind to a schema
 */
public class EvaluateVisitor implements PredicateVisitor {

    private boolean evaluate_result = false;
    private Row row = null;

    public void bindRow(Row row) {
        this.row = row;
    }

    public void unBindRow() {
        this.row = null;
    }

    public boolean getAnswer() {
        return evaluate_result;
    }

    @Override
    public void visitLessThanPredicate(LessThanPredicate lessThanPredicate) {
        Entry lhs = lessThanPredicate.lhs.getValue(this.row);
        Entry rhs = lessThanPredicate.rhs.getValue(this.row);
        evaluate_result = lhs.compareTo(rhs) < 0;
    }

    @Override
    public void visitGreaterThanPredicate(GreaterThanPredicate greaterThanPredicate) {
        Entry lhs = greaterThanPredicate.lhs.getValue(this.row);
        Entry rhs = greaterThanPredicate.rhs.getValue(this.row);
        evaluate_result = lhs.compareTo(rhs) > 0;
    }

    @Override
    public void visitGreaterEqualPredicate(GreaterEqualPredicate greaterEqualPredicate) {
        Entry lhs = greaterEqualPredicate.lhs.getValue(this.row);
        Entry rhs = greaterEqualPredicate.rhs.getValue(this.row);
        evaluate_result = lhs.compareTo(rhs) >= 0;
    }

    @Override
    public void visitLessEqualPredicate(LessEqualPredicate lessEqualPredicate) {
        Entry lhs = lessEqualPredicate.lhs.getValue(this.row);
        Entry rhs = lessEqualPredicate.rhs.getValue(this.row);
        evaluate_result = lhs.compareTo(rhs) <= 0;
    }

    @Override
    public void visitEqualPredicate(EqualPredicate equalPredicate) {
        Entry lhs = equalPredicate.lhs.getValue(this.row);
        Entry rhs = equalPredicate.rhs.getValue(this.row);
        evaluate_result = lhs.compareTo(rhs) == 0;
    }

    @Override
    public void visitNotEqualPredicate(NotEqualPredicate notEqualPredicate) {
        Entry lhs = notEqualPredicate.lhs.getValue(this.row);
        Entry rhs = notEqualPredicate.rhs.getValue(this.row);
        evaluate_result = lhs.compareTo(rhs) != 0;
    }

    @Override
    public void visitAndPredicate(AndPredicate andPredicate) {
        // evaluate the lhs result
        andPredicate.lhs.accept(this);
        if (evaluate_result) {
            // if true, evaluate the second
            andPredicate.rhs.accept(this);
        }
    }

    @Override
    public void visitOrPredicate(OrPredicate orPredicate) {
        // evaluate the lhs
        orPredicate.lhs.accept(this);
        if (!evaluate_result) {
            // if false, evaluate the second
            orPredicate.rhs.accept(this);
        }
    }

    @Override
    public void visitCompareBasePredicate(CompareBasePredicate compareBasePredicate) {
        return;
    }

    @Override
    public void visitLogicalBasePredicate(LogicalBasePredicate logicalBasePredicate) {
        return;
    }
}
