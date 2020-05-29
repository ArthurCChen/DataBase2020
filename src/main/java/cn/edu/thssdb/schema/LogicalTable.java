package cn.edu.thssdb.schema;

import java.util.ArrayList;

/**
 * A logical abstraction of table.
 */
public interface LogicalTable extends Iterable<Row> {

    boolean insert(Row row);

    boolean delete(Entry entry);

    // return true if successfully locked
    boolean lock();

    boolean is_locked();

    void unlock();

    ArrayList<Column> get_columns();
}
