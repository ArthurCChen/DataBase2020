package cn.edu.thssdb.query;

import cn.edu.thssdb.adapter.LogicalTable;
import cn.edu.thssdb.predicate.BindVisitor;
import cn.edu.thssdb.predicate.EvaluateVisitor;
import cn.edu.thssdb.predicate.Operand;
import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.predicate.logical.AndPredicate;
import cn.edu.thssdb.rpc.thrift.ExecuteStatementResp;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ValueFactory;
import cn.edu.thssdb.utils.LogBuffer;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private ExecuteStatementResp resp;


    public QueryManager(@NotNull Physical2LogicalInterface storage, @NotNull LogBuffer buffer) {
        this.storage = storage;
        this.logBuffer = buffer;
        this.shared = new HashSet<>();
        this.exclusive = new HashSet<>();
        this.current_transaction_id = -1;
        this.has_semantic_error = false;
        this.stacked_tasks = 0;
    }

    public void bind_resp(ExecuteStatementResp resp) {
        this.resp = resp;
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

    private void clear_locks(boolean isCommit) {
        for (LogicalTable table : shared) {
            table.unlock(false);
        }
        for (LogicalTable table : exclusive) {
            table.unlock(isCommit);
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

    public boolean task_clear() {
        return stacked_tasks == 0;
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
                if (current_transaction_id == -1) {
                    handle_error("RuntimeError: you are not in a transaction.");
                    over = true;
                    break;
                }
                LogicalTable table = storage.get_table(tableName, current_transaction_id);
                if (table == null) {
                    if (is_first_task()) {
                        handle_error(error_log);
                        over = true;
                    }
                    else{
                        System.out.println(stacked_tasks);
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
            TaskQueue.get_task_queue().flush();
            // as we are implementing Rigorous 2PL, release locks after transaction finish
            clear_locks(true);
        }
    }

    @Override
    public void rollback() {
        if (current_transaction_id != -1) {
            storage.abort(current_transaction_id);
            current_transaction_id = -1;
            // as we are implementing Rigorous 2PL, release locks after transaction finish
            clear_locks(false);
        }
    }

    @Override
    public void createTable(String tableName, ArrayList<Column> columns) {
        if (has_semantic_error) {
            return;
        }
        if (current_transaction_id == -1) {
            handle_error("RuntimeError: you are not in a transaction.");
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
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> {
            BindVisitor binder = new BindVisitor(logBuffer, table.get_columns());
            predicate.accept(binder);
            if (binder.has_error()) {
                handle_error("");
                return true;
            }
            EvaluateVisitor evaluator = new EvaluateVisitor();
            // a list of primary to be deleted
            ArrayList<Entry> primary = new ArrayList<>();
            ArrayList<Column> columns = table.get_columns();
            int primary_index = -1;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getPrimary()) {
                    primary_index = i;
                    break;
                }
            }
            for (Row row : table) {
                evaluator.bindRow(new MultiRow(row));
                predicate.accept(evaluator);
                if (evaluator.getAnswer()) {
                    primary.add(row.getEntries().get(primary_index));
                }
            }
            for (Entry p : primary) {
                storage.delete_row(tableName, p, current_transaction_id);
            }
            return true;
        };
        String error_log = "SemanticError: can not find table " + tableName + ".";
        BooleanSupplier task = build_task(tableName, func, false, error_log);
        submit_task(task);
    }

    @Override
    public void dropTable(String tableName) {
        if (has_semantic_error) {
            return;
        }
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> storage.drop_table(tableName, current_transaction_id);
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
            List<String> column_list = new ArrayList<>();
            for (Column column : columns) {
                column_list.add(column.getName());
            }
            List<List<String>> rows_list = new ArrayList<>();
            for (Row row : table) {
                List<String> rows_str = new ArrayList<>();
                for (Entry e : row.getEntries()) {
                    rows_str.add(e.value.toString());
                }
                rows_list.add(rows_str);
            }
            if (resp != null) {
                resp.setColumnsList(column_list);
                resp.setRowList(rows_list);
                resp.setHasResult(true);
            }
            else {
                System.out.println(column_list);
                System.out.println(rows_list);
            }
            return true;
        };
        BooleanSupplier task = build_task(tableName, func, true, error_log);
        submit_task(task);
    }

    @Override
    public void insertRow(String tableName, ArrayList<String> attr_names, ArrayList<ArrayList<Object>> entries) {
        if (has_semantic_error) {
            return;
        }
        String error_log = "SemanticError: can not insert to a non-exist table.";
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> {
            RowDesc rowDesc;
            rowDesc = new RowDesc(table.get_columns());
            ArrayList<String> names;
            if (attr_names == null) {
                names = new ArrayList<>();
                for (Column column : table.get_columns()) {
                    names.add(column.getName());
                }
            }
            else {
                names = attr_names;
            }
            ArrayList<ArrayList<Object>> rows = new ArrayList<>();
            for (ArrayList<Object> entry : entries) {
                try {
                    Row row = new Row(rowDesc, names, entry);
                    if (!storage.insert_row(tableName, row, current_transaction_id)) {
                        handle_error("SemanticError: fail to insert row " + row + " into table " + tableName + ", it already exists.");
                        break;
                    }
                } catch (Exception e) {
                    handle_error(e.getMessage());
                    break;
                }
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
        BooleanSupplier task = () -> {
            boolean over = false;
            for (;;) {
                if (has_semantic_error) {
                    over = true;
                    break;
                }
                if (current_transaction_id == -1) {
                    handle_error("RuntimeError: you are not in a transaction.");
                    over = true;
                    break;
                }
                // get all related tables
                ArrayList<LogicalTable> tables = new ArrayList<>();
                for (String table_name : vt.tables) {
                    LogicalTable table = storage.get_table(table_name, current_transaction_id);
                    if (table == null) {
                        handle_error("SemanticError: table name not found.");
                        over = true;
                        break;
                    }
                    tables.add(table);
                }
                // fail to fetch some tables
                if (tables.size() != vt.tables.size()) {
                    break;
                }
                // try lock all related tables
                boolean locked = true;
                for (int i = 0; i < tables.size(); i++) {
                    if (!require_shared_lock(tables.get(i))) {
                        for (int j = 0; j < i; j++) {
                            tables.get(j).unlock(false);
                            shared.remove(tables.get(j));
                        }
                        locked = false;
                        break;
                    }
                }
                if (!locked) {
                    break;
                }
                // get all conditions
                Predicate all_condition;
                if (conditions != null && vt.condition != null) {
                    all_condition = new AndPredicate(conditions, vt.condition);
                }
                else if (conditions != null) {
                    all_condition = conditions;
                }
                else {
                    all_condition = vt.condition;
                }
                // bind the predicate tree to the schema
                ArrayList<Column> columns = new ArrayList<>();
                for (LogicalTable table : tables) {
                    columns.addAll(table.get_columns());
                }
                BindVisitor bind = new BindVisitor(logBuffer, columns);
                if (all_condition != null) {
                    all_condition.accept(bind);
                }
                if (bind.has_error()) {
                    handle_error("");
                    break;
                }
                // traverse
                EvaluateVisitor evaluator = new EvaluateVisitor();
                JoinIterator iterator = new JoinIterator(tables);
                ArrayList<Row> rows = new ArrayList<>();
                while (iterator.get_multi_row() != null) {
                    evaluator.bindRow(iterator.get_multi_row());
                    if (all_condition != null) {
                        all_condition.accept(evaluator);
                    }
                    if (evaluator.getAnswer()) {
                        rows.add(bind.collect(result_columns, iterator.get_multi_row()));
                    }
                    iterator.next();
                }
                // build the result
                List<String> column_names = new ArrayList<>();
                for (Column column : result_columns){
                    column_names.add(column.getName());
                }
                List<List<String>> row_list = new ArrayList<>();
                for (Row row : rows) {
                    ArrayList<String> row_str = new ArrayList<>();
                    for (Entry entry : row.getEntries()) {
                        row_str.add(entry.value.toString());
                    }
                    row_list.add(row_str);
                }
                if (resp != null) {
                    resp.setColumnsList(column_names);
                    resp.setRowList(row_list);
                    resp.setHasResult(true);
                }
                else {
                    System.out.println(column_names);
                    System.out.println(row_list);
                }
                over = true;
                break;
            }
            if (over) {
                finish_task();
            }
            return over;
        };
        submit_task(task);
    }

    @Override
    public void update(String table_name, String column_name, Operand value, Predicate condition) {
        if (has_semantic_error) {
            return;
        }
        java.util.function.Predicate<LogicalTable> func = (LogicalTable table) -> {
            BindVisitor binder = new BindVisitor(logBuffer, table.get_columns());
            condition.accept(binder);
            if (binder.has_error()) {
                handle_error("");
                return true;
            }
            EvaluateVisitor evaluator = new EvaluateVisitor();
            // a list of primary to be deleted
            ArrayList<Entry> primary = new ArrayList<>();
            ArrayList<Column> columns = table.get_columns();
            ArrayList<Row> inserts = new ArrayList<>();
            int primary_index = -1;
            int update = -1;
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getPrimary()) {
                    primary_index = i;
                }
                if (columns.get(i).getName().equals(column_name)) {
                    update = i;
                }
            }
            // when field name not found
            if (update == -1) {
                handle_error(String.format("SemanticError: column %s not found.", column_name));
                return true;
            }
            for (Row row : table) {
                evaluator.bindRow(new MultiRow(row));
                condition.accept(evaluator);
                if (evaluator.getAnswer()) {
                    primary.add(row.getEntries().get(primary_index));
                    ColumnType type = row.getEntries().get(update).value.getType();
                    Entry v = new Entry(ValueFactory.getValue(value.value_str, type, value.value_str.length()));
                    row.getEntries().set(update, v);
                    inserts.add(row);
                }
            }
            for (Entry p : primary) {
                storage.delete_row(table_name, p, current_transaction_id);
            }
            for (Row row : inserts) {
                if (!storage.insert_row(table_name, row, current_transaction_id)) {
                    handle_error("RuntimeError: fail to update all the rows.");
                    return true;
                }
            }
            return true;
        };
        String error_log = "SemanticError: can not insert to a non-exist table.";
        BooleanSupplier task = build_task(table_name, func, false, error_log);
        submit_task(task);
    }

    @Override
    public void create_database(String db_name) {
        try {
            Manager.getInstance().createDatabase(db_name);
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            handle_error("SemanticError: database already exists.");
        }
    }

    @Override
    public void use_database(String db_name) {
        try {
            Manager.getInstance().useDatabase(db_name);
        } catch (Exception e) {
            handle_error("SemanticError: database does not exist.");
        }
    }

    @Override
    public void drop_database(String db_name) {
        try {
            Manager.getInstance().deleteDatabase(db_name);
        } catch (Exception e) {
            handle_error("SemanticError: database does not exist.");
        }
    }

    private void handle_error(String error) {
        logBuffer.write(error);
        has_semantic_error = true;
        rollback();
        if (resp != null) {
            resp.setIsAbort(true);
        }
    }
}
