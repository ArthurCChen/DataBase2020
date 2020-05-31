import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.LogicalTable;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * A test class for potentially multiple implementations of cn.edu.thssdb.utils.Physical2LogicalInterface
 */
public class Physical2LogicalInterfaceTest {

    // change here to switch to other implementations
//    Physical2LogicalInterface storage = new MDBManager();
    Physical2LogicalInterface storage;

    Column c0;
    Column c1;
    Row r0;
    Row r1;
    int p0 = 0;
    int p1 = 1;

    @Before
    public void setup() {
        c0 = new Column("primary", ColumnType.INT, true, true, 0);
        c1 = new Column("data", ColumnType.STRING, false, false, 5);
        r0 = new Row(new Entry[]{new Entry(p0), new Entry("r0")});
        r1 = new Row(new Entry[]{new Entry(p1), new Entry("r1")});
    }

    private boolean create_test_table1(int transaction_id, String name) {
        ArrayList<Column> columns = new ArrayList<>();
        Column primary_key = c0;
        Column data = c1;
        columns.add(primary_key);
        columns.add(data);
        return storage.create_table(name, columns, transaction_id);
    }

    @Test
    public void create_table_test() {
        // create a table
        int transaction_id = storage.start_transaction();
        boolean success = create_test_table1(transaction_id, "table1");
        assertTrue(success);
        storage.commit(transaction_id);

        // create the same table again
        transaction_id = storage.start_transaction();
        success = create_test_table1(transaction_id, "table1");
        assertFalse(success);
        storage.commit(transaction_id);

        // create another table
        transaction_id = storage.start_transaction();
        success = create_test_table1(transaction_id, "table2");
        assertTrue(success);
        storage.commit(transaction_id);
    }

    @Test
    public void drop_table_test() {
        int transaction_id = storage.start_transaction();
        // create a table
        boolean success = create_test_table1(transaction_id, "table1");
        assertTrue(success);
        // drop the table
        success = storage.drop_table("table1", transaction_id);
        assertTrue(success);
        // drop a non-exist table
        success = storage.drop_table("not exist", transaction_id);
        assertFalse(success);
        storage.abort(transaction_id);
    }

    @Test
    public void get_table_test() {
        // create a table
        int transaction_id = storage.start_transaction();
        String name = "table1";
        boolean success = create_test_table1(transaction_id, name);
        assertTrue(success);
        LogicalTable table = storage.get_table(name, transaction_id);
        assertEquals(name, table.get_name());
        ArrayList<Column> columns = table.get_columns();
        assertEquals(2, columns.size());
        assertEquals(columns.get(0), c0);
        assertEquals(columns.get(1), c1);
        storage.commit(transaction_id);

        // get a not exist table
        transaction_id = storage.start_transaction();
        Object obj = storage.get_table("not exist", transaction_id);
        assertNull(obj);
        storage.abort(transaction_id);
    }

    @Test
    public void insert_delete_row_test() {
        // create a table
        String name = "table1";
        int transaction_id = storage.start_transaction();
        boolean success = create_test_table1(transaction_id, name);
        assertTrue(success);
        storage.commit(transaction_id);

        // insert a row
        transaction_id = storage.start_transaction();
        success = storage.insert_row(name, r0, transaction_id);
        assertTrue(success);
        LogicalTable table = storage.get_table(name, transaction_id);
        int count = 0;
        for (Row r : table) {
            assertEquals(r.toString(), r0.toString());
            count += 1;
        }
        assertEquals(1, count);
        storage.commit(transaction_id);

        // insert the same row again
        transaction_id = storage.start_transaction();
        success = storage.insert_row(name, r0, transaction_id);
        assertFalse(success);
        storage.abort(transaction_id);

        // delete the row
        transaction_id = storage.start_transaction();
        success = storage.delete_row("not exist", new Entry(p0), transaction_id);
        assertFalse(success);
        success = storage.delete_row(name, new Entry(p1), transaction_id);
        assertFalse(success);
        success = storage.delete_row(name, new Entry(p0), transaction_id);
        assertTrue(success);
        table = storage.get_table(name, transaction_id);
        count = 0;
        for (Row r : table) {
            count += 1;
        }
        assertEquals(0, count);
        storage.commit(transaction_id);
    }

    // for bonus
    @Test
    public void multi_transaction() {
        String name = "table1";
        // start two transaction
        int trans1 = storage.start_transaction();
        int trans2 = storage.start_transaction();
        create_test_table1(trans1, name);
        storage.insert_row(name, r0, trans1);
        storage.insert_row(name, r1, trans2);
        storage.commit(trans1);
        storage.abort(trans2);

        int trans3 = storage.start_transaction();
        LogicalTable table = storage.get_table(name, trans3);
        int count = 0;
        for (Row r : table) {
            assertEquals(r.toString(), r0.toString());
            count += 1;
        }
        assertEquals(1, count);
    }

}
