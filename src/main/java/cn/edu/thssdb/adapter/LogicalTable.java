package cn.edu.thssdb.adapter;

import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;

import java.util.ArrayList;

/**
 * A logical abstraction of table.
 */
public interface LogicalTable extends Iterable<Row> {

    boolean insert(Row row);

    boolean delete(Entry entry);

    // return true if successfully locked
    boolean shared_lock();

    // return true if successfully locked
    boolean exclusive_lock();

    boolean is_share_locked();

    boolean is_exclusive_locked();

    // upgrade from shared lock to exclusive lock
    // return false if: 1. currently not locked(do not upgrade); 2. is already exclusive lock;
    boolean upgrade_lock();

    void unlock();

    ArrayList<Column> get_columns();

    String get_name();
}
