package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.schema.*;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class MTable implements LogicalTable {

    private String table_name;
    private ArrayList<Column> columns;
    private int primary_index;
    private ArrayList<Row> rows;
    private String lock_state;

    public MTable(String table_name, ArrayList<Column> columns) {
        this.table_name = table_name;
        this.columns = columns;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isPrimary()) {
                this.primary_index = i;
            }
        }
        this.rows = new ArrayList<>();
        this.lock_state = "free";
    }

    int getPrimary_index() {
        return primary_index;
    }

    @Override
    public boolean insert(Row row) {
        // skip type check here
        rows.add(row);
        return true;
    }

    @Override
    public boolean delete(Entry entry) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getEntries().get(this.primary_index).equals(entry)) {
                this.rows.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shared_lock() {
        if (this.lock_state.equals("free")) {
            this.lock_state = "share";
            return true;
        }
        return false;
    }

    @Override
    public boolean exclusive_lock() {
        if (this.lock_state.equals("free")) {
            this.lock_state = "exclusive";
            return true;
        }
        return false;
    }

    @Override
    public boolean is_share_locked() {
        return lock_state.equals("share");
    }

    @Override
    public boolean is_exclusive_locked() {
        return lock_state.equals("exclusive");
    }

    @Override
    public boolean upgrade_lock() {
        if (lock_state.equals("share")) {
            lock_state = "exclusive";
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        lock_state = "free";
    }

    @Override
    public ArrayList<Column> get_columns() {
        return columns;
    }

    @Override
    public String get_name() {
        return this.table_name;
    }

    @Override
    public Iterator<Row> iterator() {
        return this.rows.iterator();
    }
}
