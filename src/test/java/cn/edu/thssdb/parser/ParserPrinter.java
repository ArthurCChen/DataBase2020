package cn.edu.thssdb.parser;

import cn.edu.thssdb.predicate.Operand;
import cn.edu.thssdb.predicate.base.Predicate;
import cn.edu.thssdb.query.QueryManagerInterface;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.VirtualTable;

import java.util.ArrayList;

/**
 * This class implements QueryManager interface,
 * it just prints out the passed parameters as it is,
 * in order to debug the parser and visitor.
 * Debug Only. Helper class for test.
 */
public class ParserPrinter implements QueryManagerInterface {

    private String result;

    public String getResult() {
        return result;
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public void startTransaction() {
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    @Override
    public void createTable(String tableName, ArrayList<Column> columns) {
        result = String.format("createTable called, table name: %s, columns: %s", tableName, columns.toString());
    }

    @Override
    public void deleteRows(String tableName, Predicate predicate) {
        result = String.format("deleteRows called, table name: %s, predicate: %s", tableName, predicate);
    }

    @Override
    public void dropTable(String tableName) {
        result = String.format("dropTable called, table name: %s", tableName);
    }

    @Override
    public void quit() {
        result = "quit called.";
    }

    @Override
    public void showTable(String tableName) {
        result = String.format("showTable called, table name: %s", tableName);
    }

    @Override
    public void insertRow(String tableName, ArrayList<String> columns, ArrayList<ArrayList<Object>> entries) {
        result = String.format("insertEntry called, table name: %s, columns: %s, entries: %s", tableName, columns, entries);
    }

    @Override
    public void select(ArrayList<Column> result_columns, VirtualTable vt, Predicate conditions) {
        result = String.format("select called, result columns: %s, virtual table: %s, conditions: %s", result_columns, vt, conditions);
    }

    @Override
    public void update(String table_name, String column_name, Operand value, Predicate condition) {
        result = String.format("update called, table name: %s, value: %s, condition: %s", table_name, column_name, condition);
    }
}
