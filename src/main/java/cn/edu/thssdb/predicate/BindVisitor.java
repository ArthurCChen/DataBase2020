package cn.edu.thssdb.predicate;

import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;
import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.predicate.logical.OrPredicate;
import cn.edu.thssdb.predicate.compare.*;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.LogBuffer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class binds a predicate tree to a schema.
 * Specifically, for each leaf node of the tree,
 * it is assigned a index in the schema.
 *
 * This visitor also performs a semantic check, if no corresponding column is found in the schema,
 * give an error log.
 * If two operands has different type, also give an error.
 *
 * TODO: The creation of hash map might be time consuming.
 */
public class BindVisitor implements PredicateVisitor {

    private LogBuffer buffer;
    private String table_name;
    private boolean has_semantic_error;
    // from column name to the index
    private HashMap<String, Integer> column_name_map;
    // from column name to the type
    private HashMap<String, ColumnType> column_type_map;

    public BindVisitor(LogBuffer buffer, ArrayList<Column> columns, String table_name) {
        this.buffer = buffer;
        this.column_name_map = new HashMap<String, Integer>();
        this.table_name = table_name;
        for (int i = 0; i < columns.size(); i++) {
            this.column_name_map.put(table_name + "." + columns.get(i).getName(), i);
            this.column_type_map.put(table_name + "." + columns.get(i).getName(),
                    columns.get(i).getType());
            if (column_name_map.containsKey(columns.get(i).getName())) {
                // has conflict
                this.column_name_map.put(columns.get(i).getName(), -1);
            }
            else {
                this.column_name_map.put(columns.get(i).getName(), i);
            }
            if (column_type_map.containsKey(columns.get(i).getName())) {
                // has conflict
                this.column_type_map.put(columns.get(i).getName(), null);
            }
            else {
                this.column_type_map.put(columns.get(i).getName(), columns.get(i).getType());
            }
        }
        has_semantic_error = false;
    }

    // return false if error is found
    private boolean column_check(String name) {
        return this.column_name_map.containsKey(name) && column_name_map.get(name) != -1;
    }

    private ColumnType get_column_type(String name) {
        return this.column_type_map.get(name);
    }

    // implementation in base class to work for all compare operators
    @Override
    public void visitCompareBasePredicate(CompareBasePredicate compareBasePredicate) {
        Operand lhs = compareBasePredicate.lhs;
        Operand rhs = compareBasePredicate.rhs;

        // check lhs
        if (!lhs.is_constant) {
            if (column_check(lhs.name)) {
                lhs.setIndex(column_name_map.get(lhs.name));
            }
            else {
                this.buffer.write(String.format("SemanticError: table %s does not have column: %s or has conflict.",
                        this.table_name, lhs.name));
                has_semantic_error = true;
            }
        }
        // check rhs
        if (!rhs.is_constant) {
            if (column_check(rhs.name)) {
                rhs.setIndex(column_name_map.get(rhs.name));
            }
            else {
                this.buffer.write(String.format("SemanticError: table %s does not have column: %s.",
                        this.table_name, rhs.name));
                has_semantic_error = true;
            }
        }
        // check type consistency
        if (this.get_column_type(lhs.name) != this.get_column_type(rhs.name)) {
            this.buffer.write(String.format("SemanticError: %s and %s have different type.",
                    rhs.name, lhs.name));
            has_semantic_error = true;
        }
    }

    // implementation in base class to work for all logical operators
    @Override
    public void visitLogicalBasePredicate(LogicalBasePredicate logicalBasePredicate) {
        logicalBasePredicate.lhs.accept(this);
        logicalBasePredicate.rhs.accept(this);
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

    @Override
    public void visitAndPredicate(AndPredicate andPredicate) {
        visitLogicalBasePredicate(andPredicate);
    }

    @Override
    public void visitOrPredicate(OrPredicate orPredicate) {
        visitLogicalBasePredicate(orPredicate);
    }
}
