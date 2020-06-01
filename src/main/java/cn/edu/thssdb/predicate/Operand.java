package cn.edu.thssdb.predicate;

import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;

/**
 * Leaf node of the predicate tree
 */
public class Operand {

    private int index = -1;

    public String value_str = null;
    public String name = null;
    public String table_name = null;
    public boolean bind = false;
    public Entry value = null;
    public Boolean is_number = null;
    public boolean is_constant;

    public Operand(boolean is_constant) {
        this.is_constant = is_constant;
    }

    public Entry getValue() {
        return this.value;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    Entry getValue(Row row) {
        if (this.is_constant) {
            assert(bind);
            return this.value;
        }
        else {
            return row.getEntries().get(this.index);
        }
    }

    String get_full_name() {
        if (table_name == null) {
            return name;
        }
        else {
            return table_name + "." + name;
        }
    }

    @Override
    public String toString() {
        if (is_constant) {
            return String.format("Constant operand: %s", value.toString());
        }
        else {
            return String.format("Column operand: %s", name);
        }
    }
}
