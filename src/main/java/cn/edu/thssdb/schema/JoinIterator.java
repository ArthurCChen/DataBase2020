package cn.edu.thssdb.schema;

import cn.edu.thssdb.adapter.LogicalTable;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class JoinIterator {

    private ArrayList<LogicalTable> sources;
    private ArrayList<Iterator<Row>> current;

    public MultiRow get_multi_row() {
        return multiRow;
    }

    private MultiRow multiRow = null;

    public JoinIterator(ArrayList<LogicalTable> sources) {
        this.sources = sources;
        current = new ArrayList<>();
        ArrayList<Row> rows = new ArrayList<>();
        // add iterator, rows
        for (LogicalTable table : sources) {
            Iterator<Row> it = table.iterator();
            if (it.hasNext()) {
                rows.add(it.next());
            }
            current.add(it);
        }
        // if all tables have at least 1 row
        if (rows.size() == sources.size()) {
            if (rows.size() == 1) {
                this.multiRow = new MultiRow(rows.get(0));
            }
            else {
                HashMap<Integer, Pair<Integer, Integer>> map = MultiRow.make_map(rows);
                this.multiRow = new MultiRow(rows, map);
            }
        }
    }

    // return null if no next
    public void next() {
        for (int i = 0; i < current.size(); i++) {
            Iterator<Row> it = current.get(i);
            if (it.hasNext()) {
                Row row = it.next();
                multiRow.update(i, row);
                break;
            }
            else {
                Iterator<Row> first = sources.get(i).iterator();
                Row first_row = first.next();
                current.set(i, first);
                multiRow.update(i, first_row);
                if (i == current.size() - 1) {
                    multiRow = null;
                    break;
                }
            }
        }
    }
}
