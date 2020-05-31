package cn.edu.thssdb.query;

import cn.edu.thssdb.predicate.Operand;
import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.utils.LogBuffer;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BooleanSupplier;

public class QueryManager implements QueryManagerInterface {

    Physical2LogicalInterface storage;
    private LogBuffer logBuffer = null;
    int current_transaction_id;
    boolean has_semantic_error;
    private HashSet<LogicalTable> shared;
    private HashSet<LogicalTable> exclusive;
    // the number of tasks in the queue and not executed
    private int stacked_tasks;

    public QueryManager(@NotNull Physical2LogicalInterface storage, @NotNull LogBuffer buffer) {
        this.storage = storage;
        this.logBuffer = buffer;
        this.current_transaction_id = -1;
        this.has_semantic_error = false;
        this.shared = new HashSet<>();
        this.exclusive = new HashSet<>();
        this.stacked_tasks = 0;
    }

    public void reset() {
        this.has_semantic_error= false;
    }

    private boolean require_shared_lock(LogicalTable table) {
        assert(table != null);
        // if it already has the privilege
        if (shared.contains(table) || exclusive.contains(table)) {
            return true;
        }
        if (table.shared_lock()) {
            shared.add(table);
            return true;
        }
        return false;
    }

    private boolean require_exclusive_lock(LogicalTable table) {
        assert(table != null);
        // if it already has the privilege
        if (exclusive.contains(table)) {
            return true;
        }
        // if it owns shared lock to this table, try update it
        if (shared.contains(table) && table.upgrade_lock()) {
            shared.remove(table);
            exclusive.add(table);
            return true;
        }
        if (table.exclusive_lock()) {
            exclusive.add(table);
            return true;
        }
        return false;
    }

    private void clear_locks() {
        for (LogicalTable table : shared) {
            table.unlock();
        }
        for (LogicalTable table : exclusive) {
            table.unlock();
        }
        shared.clear();
        exclusive.clear();
    }

    private void submit_task(BooleanSupplier task) {
        TaskQueue.get_task_queue().add_task(task);
        stacked_tasks += 1;
    }

    private boolean is_first_task() {
        return stacked_tasks == 1;
    }

    private void finish_task() {
        stacked_tasks -= 1;
    }

    @Override
    public boolean ready() {
        return storage != null;
    }

    @Override
    public void startTransaction() {
        if (has_semantic_error) {
            return;
        }
        if (current_transaction_id == -1) {
            current_transaction_id = storage.start_transaction();
        }
    }

    @Override
    public void commit() {
        if (has_semantic_error) {
            return;
        }
        if (current_transaction_id != -1) {
            storage.commit(current_transaction_id);
            current_transaction_id = -1;
            // as we are implementing Rigorous 2PL, release locks after transaction finish
            clear_locks();
        }
    }

    @Override
    public void rollback() {
        if (current_transaction_id != -1) {
            storage.abort(current_transaction_id);
            current_transaction_id = -1;
            // as we are implementing Rigorous 2PL, release locks after transaction finish
            clear_locks();
        }
    }

    @Override
    public void createTable(String tableName, ArrayList<Column> columns) {
        if (has_semantic_error) {
            return;
        }
        // carry out the task in current thread, since it do not have conflict
        if (this.storage.get_table(tableName, current_transaction_id) == null) {
            this.storage.create_table(tableName, columns, current_transaction_id);
            LogicalTable table = storage.get_table(tableName, current_transaction_id);
            boolean suc = require_shared_lock(table);
            assert(suc);
        }
        else {
            handle_error("SemanticError: the table already exists.");
        }
    }

    @Override
    public void deleteRows(String tableName, Predicate predicate) {
        if (has_semantic_error) {
            return;
        }
    }

    @Override
    public void dropTable(String tableName) {
        if (has_semantic_error) {
            return;
        }
        BooleanSupplier task = () -> {
            if (has_semantic_error) {
                finish_task();
                return true;
            }
            LogicalTable table = storage.get_table(tableName, current_transaction_id);
            if (table == null) {
                if (is_first_task()) {
                    finish_task();
                    handle_error("SemanticError: can not drop a non-exist table.");
                    return true;
                }
                else {
                    return false;
                }
            }
            if (!require_shared_lock(table)) {
                return false;
            }
            storage.drop_table(tableName, current_transaction_id);
            return true;
        };
        submit_task(task);
    }

    @Override
    public void quit() {
        if (has_semantic_error) {
            return;
        }
        if (current_transaction_id != -1) {
            commit();
        }
        // something else
    }

    @Override
    public void showTable(String tableName) {
        if (has_semantic_error) {
            return;
        }
        BooleanSupplier task = () -> {
            if (has_semantic_error) {
                finish_task();
                return true;
            }
            LogicalTable table = storage.get_table(tableName, current_transaction_id);
            if (table == null) {
                if (is_first_task()) {
                    finish_task();
                    handle_error("SemanticError: can not show a non-exist table.");
                    return true;
                }
                else {
                    return false;
                }
            }
            if (!require_shared_lock(table)) {
                return false;
            }
            // now show by print it out
            ArrayList<Column> columns = table.get_columns();
            System.out.println("Show table: " + tableName);
            StringBuilder header = new StringBuilder();
            header.append("\t");
            for (Column column : columns) {
                header.append(column.getName()).append("\t");
            }
            System.out.println(header);
            for (Row row : table) {
                StringBuilder r = new StringBuilder();
                r.append("\t");
                for (Entry e : row.getEntries()) {
                    r.append(e.value).append("\t");
                }
                System.out.println(r);
            }
            System.out.println("End table");
            finish_task();
            return true;
        };
        submit_task(task);
    }

    @Override
    public void insertRow(String tableName, ArrayList<Column> columns, ArrayList<Row> entries) {
        if (has_semantic_error) {
            return;
        }
        LogicalTable table = storage.get_table(tableName, current_transaction_id);
        if (table == null) {
            this.handle_error("SemanticError: can not insert to a non-exist table.");
            return;
        }

    }

    @Override
    public void select(ArrayList<Column> result_columns, VirtualTable vt, Predicate conditions) {
        if (has_semantic_error) {
            return;
        }
    }

    @Override
    public void update(String table_name, String column_name, Operand value, Predicate condition) {
        if (has_semantic_error) {
            return;
        }
    }

    private void handle_error(String error) {
        logBuffer.write(error);
        has_semantic_error = true;
        rollback();
    }
}
