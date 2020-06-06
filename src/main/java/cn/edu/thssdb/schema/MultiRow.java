package cn.edu.thssdb.schema;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiRow {

    // case1: more than one row
    private ArrayList<Row> rows = null;
    private HashMap<Integer, Pair<Integer, Integer>> table = null;

    // case2: one row
    private ArrayList<Entry> row = null;

    public static HashMap<Integer, Pair<Integer, Integer>> make_map(ArrayList<Row> rows) {
        HashMap<Integer, Pair<Integer, Integer>> table = new HashMap<>();
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            ArrayList<Entry> row = rows.get(i).getEntries();
            for (int j = 0; j < row.size(); j++) {
                table.put(index, new Pair<>(i, j));
                index += 1;
            }
        }
        return table;
    }

    public MultiRow(ArrayList<Row> rows, HashMap<Integer, Pair<Integer, Integer>> table) {
        this.rows = rows;
        this.table = table;
    }

    public MultiRow(Row row) {
        this.row = row.getEntries();
    }

    public Entry get(int i) {
        // skip boundary check for efficiency
        if (this.rows == null) {
            return row.get(i);
        }
        else {
            Pair<Integer, Integer> index = this.table.get(i);
            return rows.get(index.getKey()).getEntries().get(index.getValue());
        }
    }

    public void update(int i, Row row) {
        if (rows == null) {
            this.row = row.getEntries();
        }
        else {
            rows.set(i, row);
        }
    }
}
