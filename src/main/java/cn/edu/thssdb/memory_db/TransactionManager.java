package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.adapter.LogicalTable;
import cn.edu.thssdb.adapter.ReferenceInterface;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * This is a implementation of cn.edu.thssdb.utils.Physical2LogicalInterface,
 * it handles transaction
 */
public class TransactionManager implements Physical2LogicalInterface {

    Physical2LogicalInterface storage_manager;
    // an transaction is actually made up of several tasks which is to be executed when commit.
    // (such as writing log)
    HashMap<Integer, ArrayList<Consumer<Boolean>>> transaction_pool;
    HashMap<String, Integer> primary_index_cache;
    int max_transaction_id;

    public TransactionManager() {
        storage_manager = ReferenceInterface.getInstance();
        this.transaction_pool = new HashMap<>();
        this.max_transaction_id = 0;
        primary_index_cache = new HashMap<>();
    }

    @Override
    public boolean create_table(String table_name, ArrayList<Column> columns, int transaction_id) {
        boolean success = storage_manager.create_table(table_name, columns, -1);
        if (success) {
            // an action to be executed after commit or abort
            this.transaction_pool.get(transaction_id).add((commit) -> {
                if (commit) {
                    System.out.println("table " + table_name + " created in transaction " + transaction_id);
                }
                else {
                    // when abort, remove the table
                    storage_manager.drop_table(table_name, -1);
                }
            });
        }
        return success;
    }

    @Override
    public boolean drop_table(String table_name, int transaction_id) {
        LogicalTable table = storage_manager.get_table(table_name, -1);
        primary_index_cache.remove(table_name);
        if (table != null) {
            storage_manager.drop_table(table_name, -1);
            this.transaction_pool.get(transaction_id).add((success) -> {
                if (success) {
                    System.out.println("drop table " + table_name);
                }
                else {
                    // when abort, add the table back again
                    storage_manager.create_table(table_name, table.get_columns(), -1);
                }
            });
        }
        return table != null;
    }

    @Override
    public LogicalTable get_table(String table_name, int transaction_id) {
        return storage_manager.get_table(table_name, -1);
    }

    private int get_primary_index(String table_name) {
        if (!primary_index_cache.containsKey(table_name)) {
            ArrayList<Column> columns = storage_manager.get_table(table_name, -1).get_columns();
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getPrimary()) {
                    primary_index_cache.put(table_name, i);
                    break;
                }
            }
        }
        return primary_index_cache.get(table_name);
    }

    @Override
    public boolean insert_row(String table_name, Row row, int transaction_id) {
        boolean success_insert = storage_manager.insert_row(table_name, row, -1);
        if (success_insert) {
            this.transaction_pool.get(transaction_id).add((success) -> {
                if (success) {
                    System.out.println("insert a row: " + row + " to table: " + table_name);
                }
                else {
                    int primary_index = get_primary_index(table_name);
                    storage_manager.delete_row(table_name, row.getEntries().get(primary_index), -1);
                }
            });
        }
        return success_insert;
    }

    @Override
    public boolean delete_row(String table_name, Entry primary_key, int transaction_id) {
        LogicalTable table = storage_manager.get_table(table_name, -1);
        if (table != null) {
            Row row = null;
            int primary_index = get_primary_index(table_name);
            for (Row r : table) {
                if (r.getEntries().get(primary_index).equals(primary_key)) {
                    row = r;
                    break;
                }
            }
            final Row to_be_delete = row;
            boolean delete_success = storage_manager.delete_row(table_name, primary_key, -1);
            if (delete_success) {
                this.transaction_pool.get(transaction_id).add((success) -> {
                    if (success) {
                        System.out.println("delete a row with primary key: " + primary_key + " from table: " + table_name);
                    }
                    else {
                        // add the row back
                        storage_manager.insert_row(table_name, to_be_delete, -1);
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
        this.transaction_pool.put(this.max_transaction_id, new ArrayList<>());
        max_transaction_id += 1;
        return max_transaction_id - 1;
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