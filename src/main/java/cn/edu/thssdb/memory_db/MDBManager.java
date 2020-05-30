package cn.edu.thssdb.memory_db;

import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.utils.Physical2LogicalInterface;

import java.util.ArrayList;

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

    @Override
    public boolean create_table(String table_name, ArrayList<Column> columns, int transaction_id) {
        for (int i = 0; i < this.tables.size(); i++) {
            if (this.tables.)
        }
        return false;
    }

    @Override
    public boolean drop_table(String table_name, int transaction_id) {
        return false;
    }

    @Override
    public MTable get_table(String table_name, int transaction_id) {
        return null;
    }

    @Override
    public boolean insert_row(String table_name, Row row, int transaction_id) {
        return false;
    }

    @Override
    public boolean delete_row(String table_name, Entry primary_key, int transaction_id) {
        return false;
    }

    @Override
    public Row search(String table_name, Entry primary_key, int transaction_id) {
        return null;
    }

    @Override
    public int start_transaction() {
        return 0;
    }

    @Override
    public boolean abort(int transaction_id) {
        return false;
    }

    @Override
    public boolean commit(int transaction_id) {
        return false;
    }
}
