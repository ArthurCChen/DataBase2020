package cn.edu.thssdb.utils;

import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;

import java.util.ArrayList;

/**
 * This is an interface to bridge the gap between physical operation and logical plan.
 * Below this interface is code about data operation, including table manipulation, and user management.
 * Above this interface is code about organizing the process of request parsing, plan making, task dispatching and responding.
 */
public interface Physical2LogicalInterface {
    // create a table, cn.edu.thssdb.schema.Column is different from sample code form TA
    // transaction_id can be ignored  for now, but it is related to a bonus of 3 points.
    // return true when success
    boolean create_table(String table_name, ArrayList<Column> columns, int transaction_id);

    // drop a table
    // return true when success
    boolean drop_table(String table_name, int transaction_id);

    // get a table, since cn.edu.thssdb.schema.Table implements iterable,
    // this can serve as the requested O(1) iterator
    // return null if the table is not found
    Table get_table(String table_name, int transaction_id);

    // return true when success
    boolean insert_row(String table_name, Row row, int transaction_id);

    // return true when success
    boolean delete_row(String table_name, Entry primary_key, int transaction_id);

    // search a row based on primary key
    Row search(String table_name, Entry primary_key, int transaction_id);

    // return the transaction id
    // always return 0 if transaction not implemented
    int start_transaction();

    // return true if success
    boolean abort(int transaction_id);

    // return true if success
    // !! for efficiency reason, please do not do any disk writing before commit !!
    boolean commit(int transaction_id);

}
