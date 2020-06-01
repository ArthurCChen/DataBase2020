package cn.edu.thssdb.query;

import cn.edu.thssdb.adapter.LogicalTable;
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
    private HashSet<LogicalTable> shared;
    private HashSet<LogicalTable> exclusive;
    private int current_transaction_id;
    private boolean has_semantic_error;
    // the number of tasks in the queue and not executed
    private Integer stacked_tasks;


    public QueryManager(@NotNull Physical2LogicalInterface storage, @NotNull LogBuffer buffer) {
        this.storage = storage;
        this.logBuffer = buffer;
        this.shared = new HashSet<>();
        this.exclusive = new HashSet<>();
        this.current_transaction_id = -1;
        this.has_semantic_error = false;
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
        stacked_tasks += 1;
        TaskQueue.get_task_queue().add_task(task);
    }

    private boolean is_first_task() {
        return stacked_tasks == 1;
    }

    private void finish_task() {
        stacked_tasks -= 1;
    }

    // to simplify the code
    // check semantic error, table existence, check lock,
    private BooleanSupplier build_task(String tableName, java.util.function.Predicate<LogicalTable> func,
                                       boolean shared_lock, String error_log) {
        BooleanSupplier task = () -> {
            boolean over = false;
            for (;;) {
                if (has_semantic_error) {
                    over = true;
                    break;
                }
                LogicalTable table = storage.get_table(tableName, current_transaction_id);
                if (table == null) {
                    if (is_first_task()) {
                        handle_error(error_log);
                        over = true;
                    }
                    break;
                }
                if ((shared_lock && require_shared_lock(table)) ||
                        (!shared_lock && require_exclusive_lock(table))) {
                    over = func.test(table);
                }
                break;
            }
            if (over) {
                finish_task();
            }
            return over;
        };
        return task;
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
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> {
            return storage.drop_table(tableName, current_transaction_id);
        };
        String error_log = "SemanticError: can not drop a non-exist table.";
        BooleanSupplier task = build_task(tableName, func, true, error_log);
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
        String error_log = "SemanticError: can not show a non-exist table.";
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> {
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
            return true;
        };
        BooleanSupplier task = build_task(tableName, func, true, error_log);
        submit_task(task);
    }

    @Override
    public void insertRow(String tableName, ArrayList<Column> columns, ArrayList<ArrayList<String>> entries) {
        if (has_semantic_error) {
            return;
        }
        String error_log = "SemanticError: can not insert to a non-exist table.";
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> {
            ArrayList<Row> expanded_row;
            if (columns == null) {
//                expanded_row = entries;
            }
            else {
                
            }
            return true;
        };
        BooleanSupplier task = build_task(tableName, func, false, error_log);
        submit_task(task);
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
