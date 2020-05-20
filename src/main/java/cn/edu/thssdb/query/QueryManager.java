package cn.edu.thssdb.query;

import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;

/**
 *     Usage:
 *         Each query manager is a virtual user on the server side.
 *         It records the current user's state, his privilege, and so on.
 *         The query manager doesn't do any physical work,
 *         it just check if the query made by the current user is valid, and feasible.
 *         Once the manager has validated the query,
 *         it will be added to a task queue with user specific information.
 *     Output:
 *         all the methods except ready returns a String,
 *         indicating whether the operation has been successfully added to the queue,
 *         if "OK", everything is fine; else the error is returned.
 */
public interface QueryManager {

    public boolean ready();

    // start a transaction
    public void startTransaction();

    // end a transaction
    public void summit();

    // discard all the changes that has been made within the current transaction
    public void rollback();

    public void createTable(@NotNull String tableName, @NotNull ArrayList<Column> columns);

    public void deleteRows(@NotNull String tableName, @NotNull Predicate predicate);

    public void dropTable(@NotNull String tableName);

    public void quit();

    public void showTable(@NotNull String tableName);

    public void insertEntry(@NotNull String tableName, @NotNull ArrayList<Column> columns, @NotNull ArrayList<Row> entries);
}
