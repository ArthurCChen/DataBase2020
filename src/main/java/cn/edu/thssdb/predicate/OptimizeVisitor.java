package cn.edu.thssdb.predicate;

import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;
import cn.edu.thssdb.predicate.compare.*;
import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.predicate.logical.OrPredicate;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * This visitor modifies the predicate tree to optimize search efficiency
 *
 * Optimize techniques:
 *  1. split the tree based on related table
 */
public class OptimizeVisitor implements PredicateVisitor {

    private HashMap<Integer, Pair<Integer, Integer>> position;
    public HashMap<Integer, Predicate> split;

    // work as return value
    private int related_table;
    private boolean same_table;
    private boolean first_layer;
    private boolean removed;

    public void OptimizeVisitor(HashMap<Integer, Pair<Integer, Integer>> map) {
        this.position = map;
        this.related_table = -1;
        this.first_layer = true;
        this.split = new HashMap<>();
        this.removed = false;
    }

    @Override
    public void visitCompareBasePredicate(CompareBasePredicate compareBasePredicate) {
        assert compareBasePredicate.lhs.bind : "some operand is not bind yet.";
        assert compareBasePredicate.rhs.bind : "some operand is not bind yet.";
        int table_index = -1;
        same_table = true;
        for (Operand operand : new Operand[]{compareBasePredicate.lhs, compareBasePredicate.rhs}) {
            if (operand.is_constant) {
                int index = position.get(operand.index).getKey();
                if (table_index == -1) {
                    table_index = index;
                }
                else if (table_index != index) {
                    same_table = false;
                }
            }
        }
        if (same_table) {
            related_table = table_index;
        }
    }

    @Override
    public void visitAndPredicate(AndPredicate andPredicate) {

        andPredicate.lhs.accept(this);
        int related_table_lhs = related_table;
        boolean same_table_lhs = same_table;
        andPredicate.rhs.accept(this);
        int related_table_rhs = related_table;
        boolean same_table_rhs = same_table;

        if (!first_layer) {
            if (same_table_lhs && same_table_rhs && related_table_lhs == related_table_rhs &&
                    related_table_lhs != -1) {
                same_table  = true;
                related_table = related_table_lhs;
            }
            else {
                same_table = false;
                related_table = -1;
            }
        }
        else {
            boolean left_out = false;
            boolean right_out = false;
            if (same_table_lhs) {
                if (split.containsKey(related_table_lhs)) {
                    Predicate p = new AndPredicate(split.get(related_table_lhs), andPredicate.lhs);
                    split.put(related_table_lhs, p);
                }
                else {
                    split.put(related_table_lhs, andPredicate.lhs);
                }
                left_out = true;
            }
            if (same_table_rhs) {
                if (split.containsKey(related_table_rhs)) {
                    Predicate p = new AndPredicate(split.get(related_table_rhs), andPredicate.rhs);
                    split.put(related_table_rhs, p);
                }
                else {
                    split.put(related_table_rhs, andPredicate.rhs);
                }
                right_out = true;
            }
            if (left_out) {
                
            }
        }
    }

    @Override
    public void visitOrPredicate(OrPredicate orPredicate) {

        boolean layer = first_layer;
        first_layer = false;

        orPredicate.lhs.accept(this);
        int related_table_lhs = related_table;
        boolean same_table_lhs = same_table;
        orPredicate.rhs.accept(this);
        int related_table_rhs = related_table;
        boolean same_table_rhs = same_table;

        if (same_table_lhs && same_table_rhs && related_table_lhs == related_table_rhs && related_table_lhs != -1) {
            same_table  = true;
            related_table = related_table_lhs;
        }
        else {
            same_table = false;
            related_table = -1;
        }

        first_layer = layer;
    }

    @Override
    public void visitLogicalBasePredicate(LogicalBasePredicate logicalBasePredicate) {
        // do nothing
    }

    @Override
    public void visitLessThanPredicate(LessThanPredicate lessThanPredicate) {
        visitCompareBasePredicate(lessThanPredicate);
    }

    @Override
    public void visitGreaterThanPredicate(GreaterThanPredicate greaterThanPredicate) {
        visitCompareBasePredicate(greaterThanPredicate);
    }

    @Override
    public void visitGreaterEqualPredicate(GreaterEqualPredicate greaterEqualPredicate) {
        visitCompareBasePredicate(greaterEqualPredicate);
    }

    @Override
    public void visitLessEqualPredicate(LessEqualPredicate lessEqualPredicate) {
        visitCompareBasePredicate(lessEqualPredicate);
    }

    @Override
    public void visitEqualPredicate(EqualPredicate equalPredicate) {
        visitCompareBasePredicate(equalPredicate);
    }

    @Override
    public void visitNotEqualPredicate(NotEqualPredicate notEqualPredicate) {
        visitCompareBasePredicate(notEqualPredicate);
    }
}
