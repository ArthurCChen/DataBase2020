package cn.edu.thssdb.predicate;

import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.MultiRow;
import cn.edu.thssdb.schema.Row;

/**
 * Leaf node of the predicate tree
 * When bind:
 *     for constant:
 *         set entry value
 *     for column:
 *         set index
 */
public class Operand {

    public int index = -1;
    public String value_str = null;
    public String name = null;
    public String table_name = null;

    public boolean bind = false;
    public Entry value = null;
    public boolean is_constant;

    public Operand(boolean is_constant) {
        this.is_constant = is_constant;
    }


    public void setIndex(int index) {
        bind = true;
        this.index = index;
    }

    public void setEntry(Entry entry) {
        bind = true;
        this.value = entry;
    }

    Entry getValue(MultiRow row) {
        if (this.is_constant) {
            assert(bind);
            return this.value;
        }
        else {
            return row.get(this.index);
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
        StringBuilder builder = new StringBuilder();
        if (is_constant) {
            builder.append(String.format("Constant operand: %s", value_str));
            if (bind) {
                builder.append(" with value ").append(value.toString());
            }
        }
        else {
            builder.append(String.format("Column operand: %s", name));
            if (bind) {
                builder.append(" with index ").append(index);
            }
        }
        return builder.toString();
    }
}
