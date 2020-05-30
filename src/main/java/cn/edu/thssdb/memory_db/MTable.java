package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.schema.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class MTable implements LogicalTable {

    private String table_name;
    private ArrayList<Column> columns;
    private int primary_index;
    private ArrayList<Row> rows;
    private boolean ;

    public MTable(String table_name, ArrayList<Column> columns) {
        this.table_name = table_name;
        this.columns = columns;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isPrimary()) {
                this.primary_index = i;
            }
        }
        this.rows = new ArrayList<>();

    }

    @Override
    public boolean insert(Row row) {
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
    public boolean lock() {
        return false;
    }

    @Override
    public boolean is_locked() {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public ArrayList<Column> get_columns() {
        return null;
    }

    @Override
    public Iterator<Row> iterator() {
        return null;
    }
}
