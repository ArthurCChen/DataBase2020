package cn.edu.thssdb.predicate;

import cn.edu.thssdb.schema.Entry;

/**
 * Leaf node of the predicate tree
 */
public class Operand {

    public String name = null;
    public boolean is_constant;
    private Entry value = null;
    // if this is a
    private int index = -1;

    public Operand(boolean is_constant) {
        this.is_constant = is_constant;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Entry value) {
        this.value = value;
    }

    public Entry getValue() {
        return this.value;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    Entry getValue(Row row) {
        if (this.is_constant) {
            return this.value;
        }
        else {
            return row.getEntries().get(this.index);
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
