package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.adapter.LogicalTable;
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
    /**
     * lock state encoded as follows:
     *     = 0: free
     *     >0 : times of shared lock
     *     = -1: exclusive lock
     */
    private int lock_state;

    public MTable(String table_name, ArrayList<Column> columns) {
        this.table_name = table_name;
        this.columns = columns;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getPrimary()) {
                this.primary_index = i;
            }
        }
        this.rows = new ArrayList<>();
        this.lock_state = 0;
    }

    int getPrimary_index() {
        return primary_index;
    }

    @Override
    public boolean insert(Row row) {
        // skip type check here
        Entry value = row.getEntries().get(primary_index);
        // sanity check
        for (Row r : rows) {
            if (r.getEntries().get(primary_index).equals(value)) {
                return false;
            }
        }
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
        if (this.lock_state >= 0) {
            this.lock_state += 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean exclusive_lock() {
        if (this.lock_state == 0) {
            this.lock_state = -1;
            return true;
        }
        return false;
    }

    @Override
    public boolean is_share_locked() {
        return lock_state > 0;
    }

    @Override
    public boolean is_exclusive_locked() {
        return lock_state == -1;
    }

    @Override
    public boolean upgrade_lock() {
        // there is only one shared lock
        // need to assume the caller is the owner of the shared lock
        if (lock_state == 1) {
            lock_state = -1;
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        if (lock_state > 0) {
            lock_state -= 1;
        }
        else {
            lock_state = 0;
        }
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
