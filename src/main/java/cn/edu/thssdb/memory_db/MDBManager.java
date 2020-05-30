package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import jdk.nashorn.internal.ir.Block;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

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
    // an transaction is actually made up of several tasks which is to be executed when commit.
    // (such as writing log)
    ArrayList<ArrayList<Consumer<Boolean>>> transaction_pool;


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
            if (column.isPrimary()) {
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
            // an action to be executed after commit or abort
            this.transaction_pool.get(transaction_id).add((success) -> {
                if (success) {
                    System.out.println("table " + table_name + " created in transaction " + transaction_id);
                }
                else {
                    // when abort, remove the table
                    this.tables.remove(new_table);
                }
            });
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
            this.transaction_pool.get(transaction_id).add((success) -> {
                if (success) {
                    System.out.println("drop table " + table_name);
                }
                else {
                    // when abort, add the table back again
                    this.tables.add(table);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public MTable get_table(String table_name, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            this.transaction_pool.get(transaction_id).add((success) -> {
                if (success) {
                    System.out.println("table " + table_name + " requested");
                }
                else {
                    // do not need to do anything, since we assume the table is not changed
                }
            });
            return this.tables.get(table_index);
        }
        return null;
    }

    @Override
    public boolean insert_row(String table_name, Row row, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            this.tables.get(table_index).insert(row);
            this.transaction_pool.get(transaction_id).add((success) -> {
                if (success) {
                    System.out.println("insert a row: " + row + " to table: " + table_name);
                }
                else {
                    // remove the row
                    this.tables.remove(row);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean delete_row(String table_name, Entry primary_key, int transaction_id) {
        int table_index = get_table_index(table_name);
        if (table_index != -1) {
            Row row = null;
            int primary_index = this.tables.get(table_index).getPrimary_index();
            for (Row r : this.tables.get(table_index)) {
                if (r.getEntries().get(primary_index).equals(primary_key)) {
                    row = r;
                    break;
                }
            }
            final Row to_be_delete = row;
            boolean delete_success = this.tables.get(table_index).delete(primary_key);
            if (delete_success) {
                this.transaction_pool.get(transaction_id).add((success) -> {
                    if (success) {
                        System.out.println("delete a row with primary key: " + primary_key + " from table: " + table_name);
                    }
                    else {
                        // add the row back
                        this.tables.get(table_index).insert(to_be_delete);
                    }
                });
                return true;
            }
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
        this.transaction_pool.add(new ArrayList<>());
        return transaction_pool.size() - 1;
    }

    @Override
    public boolean abort(int transaction_id) {
        ArrayList<Consumer<Boolean>> transaction = transaction_pool.get(transaction_id);
        // undo each operation in reverse order
        for (int i = transaction.size() - 1; i >= 0 ; i--) {
            transaction.get(i).accept(false);
        }
        transaction_pool.remove(transaction_id);
        return true;
    }

    @Override
    public boolean commit(int transaction_id) {
        ArrayList<Consumer<Boolean>> transaction = transaction_pool.get(transaction_id);
        for (int i = 0; i < transaction.size() ; i++) {
            transaction.get(i).accept(true);
        }
        transaction_pool.remove(transaction_id);
        return false;
    }
}