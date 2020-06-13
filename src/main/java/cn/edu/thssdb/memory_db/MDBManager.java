package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.utils.Physical2LogicalInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * This is a implementation of cn.edu.thssdb.utils.Physical2LogicalInterface,
 * It is for logical layer's test only.
 * Please do not call methods of this class from physical layer.
 *
 * This class stands alone as an independent and minimal physical layer.
 * It guarantees correctness, but the efficiency can be ugly.
 *
 * For physical layer developers, you can check correctness by comparing output with this implementation.
 */
public class MDBManager implements Physical2LogicalInterface {

    ArrayList<MTable> tables;

    public MDBManager() {
        this.tables = new ArrayList<>();
    }


    int get_table_index(String table_name) {
        // check name exists
        for (MTable table : this.tables) {
            if (table.get_name().equals(table_name)) {
                return this.tables.indexOf(table);
            }
        }
        return -1;
    }

    boolean primary_key_ok(ArrayList<Column> columns) {
        boolean has_primary = false;
        for (Column column : columns) {
            if (column.getPrimary()) {
                if (has_primary) {
                    // multiple primary key
                    return false;
                }
                else {
                    has_primary = true;
                }
            }
        }
        return has_primary;
    }

    @Override
    public boolean create_table(String table_name, ArrayList<Column> columns, int transaction_id) {
        int table_index = get_table_index(table_name);
        boolean has_one_primary_key = primary_key_ok(columns);
        // create the table
        if (table_index == -1 && has_one_primary_key) {
            MTable new_table = new MTable(table_name, columns);
            this.tables.add(new_table);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean drop_table(String table_name, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            MTable table = this.tables.get(table_index);
            this.tables.remove(table_index);
            return true;
        }
        return false;
    }

    @Override
    public MTable get_table(String table_name, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            return this.tables.get(table_index);
        }
        return null;
    }

    @Override
    public boolean insert_row(String table_name, Row row, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            return this.tables.get(table_index).insert(row);
        }
        return false;
    }

    @Override
    public boolean delete_row(String table_name, Entry primary_key, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            int primary_index = this.tables.get(table_index).getPrimary_index();
            for (Row r : this.tables.get(table_index)) {
                if (r.getEntries().get(primary_index).equals(primary_key)) {
                    break;
                }
            }
            return this.tables.get(table_index).delete(primary_key);
        }
        return false;
    }

    // disabled, replace it by get_table
    @Override
    public Row search(String table_name, Entry primary_key, int transaction_id) {
        return null;
    }

    @Override
    public int start_transaction() {
        // should not execute here
        return -1;
    }

    @Override
    public int start_transaction(int id) {
        return 0;
    }

    @Override
    public boolean abort(int transaction_id) {
        // should not execute here
        return true;
    }

    @Override
    public boolean commit(int transaction_id) {
        // should not execute here
        return true;
    }
}