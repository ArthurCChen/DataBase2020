package cn.edu.thssdb.predicate;

import cn.edu.thssdb.predicate.base.CompareBasePredicate;
import cn.edu.thssdb.predicate.base.LogicalBasePredicate;
import cn.edu.thssdb.predicate.base.PredicateVisitor;
import cn.edu.thssdb.predicate.compare.*;
import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.predicate.logical.OrPredicate;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.MultiRow;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ValueFactory;
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

    public BindVisitor(LogBuffer buffer, ArrayList<Column> columns) {
        this.buffer = buffer;
        this.column_name_map = new HashMap<>();
        this.column_type_map = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            this.column_name_map.put(columns.get(i).getFullName(), i);
            this.column_type_map.put(columns.get(i).getFullName(),
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

    public Row collect(ArrayList<Column> columns, MultiRow original) {
        ArrayList<Entry> result = new ArrayList<>();
        for (Column column : columns) {
            result.add(original.get(column_name_map.get(column.getFullName())));
        }
        return new Row(result);
    }

    public boolean has_error() {
        return this.has_semantic_error;
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
        ColumnType type = null;

        Operand constant = null, column = null;

        // count the number of constants
        int count = 0;

        // check lhs
        if (!lhs.is_constant) {
            count += 1;
            column = lhs;
            if (column_check(lhs.get_full_name())) {
                lhs.setIndex(column_name_map.get(lhs.get_full_name()));
                type = get_column_type(lhs.get_full_name());
            }
            else {
                this.buffer.write(String.format("SemanticError: table does not have column: %s or has conflict.",
                        lhs.get_full_name()));
                has_semantic_error = true;
            }
        }
        else {
            constant = lhs;
        }
        // check rhs
        if (!rhs.is_constant) {
            count += 1;
            column = rhs;
            if (column_check(rhs.get_full_name())) {
                rhs.setIndex(column_name_map.get(rhs.get_full_name()));
                if (count == 2 && get_column_type(rhs.get_full_name()) != type) {
                    this.buffer.write(String.format("SemanticError: %s and %s have different type.",
                            rhs.get_full_name(), lhs.get_full_name()));
                    has_semantic_error = true;
                }
            }
            else {
                this.buffer.write(String.format("SemanticError: table does not have column: %s.",
                        rhs.get_full_name()));
                has_semantic_error = true;
            }
        }
        else {
            constant = rhs;
        }
        if (!has_semantic_error) {
            // one constant, one column
            if (count == 1) {
                constant.setEntry(new Entry(ValueFactory.getValue(constant.value_str, type, constant.value_str.length())));
            }
            // two constants
            else if (count == 0) {
                boolean lhs_str = false;
                boolean rhs_str = false;
                Double num_lhs = 0.0, num_rhs = 0.0;
                try {
                    num_lhs = Double.parseDouble(lhs.value_str);
                } catch (NumberFormatException e) {
                    lhs_str = true;
                }
                try {
                    num_rhs = Double.parseDouble(rhs.value_str);
                } catch (NumberFormatException e) {
                    rhs_str = true;
                }
                if (lhs_str && rhs_str) {
                    lhs.setEntry(new Entry(ValueFactory.getValue(lhs.value_str, ColumnType.STRING, lhs.value_str.length())));
                    rhs.setEntry(new Entry(ValueFactory.getValue(rhs.value_str, ColumnType.STRING, rhs.value_str.length())));
                }
                else if (!lhs_str && !rhs_str) {
                    lhs.setEntry(new Entry(ValueFactory.getValue(num_lhs, ColumnType.DOUBLE, 0)));
                    rhs.setEntry(new Entry(ValueFactory.getValue(num_rhs, ColumnType.DOUBLE, 0)));
                }
                else {
                    this.buffer.write("SemanticError: " + lhs.value_str + " and " + rhs.value_str + " have different types.");
                    has_semantic_error = true;
                }
            }
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
