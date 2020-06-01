import cn.edu.thssdb.adapter.ReferenceInterface;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.adapter.LogicalTable;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ValueFactory;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
    Integer p0 = 0;
    Integer p1 = 1;

    public Physical2LogicalInterfaceTest(){
        try {
            storage = ReferenceInterface.getInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {

        c0 = new Column("primary", ColumnType.INT, true, true, 0);
        c1 = new Column("data", ColumnType.STRING, false, false, 5);

        RowDesc rowDesc = new RowDesc(new ArrayList<Column>(Arrays.asList(c0, c1)));
        try {
            r0 = new Row(rowDesc,
                    new ArrayList<String>(Arrays.asList("primary", "data")),
                    new ArrayList<Object>(Arrays.asList(p0, new String("r0"))));
            r1 = new Row(rowDesc,
                    new ArrayList<String>(Arrays.asList("primary", "data")),
                    new ArrayList<Object>(Arrays.asList(p1, new String("r1"))));
        }catch (Exception e){
            e.printStackTrace();
        }
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

        // create a table and abort
        transaction_id = storage.start_transaction();
        success = create_test_table1(transaction_id, "table3");
        assertTrue(success);
        storage.abort(transaction_id);
        transaction_id = storage.start_transaction();
        LogicalTable table = storage.get_table("table3", transaction_id);
        assertNull(table);
        storage.abort(transaction_id);

        // create that table again
        transaction_id = storage.start_transaction();
        success = create_test_table1(transaction_id, "table3");
        assertTrue(success);
        storage.commit(transaction_id);
    }

    @Test
    public void drop_table_test() {
        int transaction_id = storage.start_transaction();
        // create a table
        boolean success = create_test_table1(transaction_id, "table10");
        assertTrue(success);
        // drop the table
        success = storage.drop_table("table10", transaction_id);
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
        String name = "table4";
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
        String name = "table5";
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
        success = storage.delete_row("not exist", new Entry(ValueFactory.getValue(p0)), transaction_id);
        assertFalse(success);
        success = storage.delete_row(name, new Entry(ValueFactory.getValue(p1)), transaction_id);
        assertFalse(success);
        success = storage.delete_row(name, new Entry(ValueFactory.getValue(p0)), transaction_id);
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
        boolean b1 = storage.insert_row(name, r0, trans1);
        boolean b2 = storage.insert_row(name, r1, trans2);
        storage.commit(trans1);
        storage.abort(trans2);

        int trans3 = storage.start_transaction();
        LogicalTable table = storage.get_table(name, trans3);
        int count = 0;
        for (Row r : table) {
            count += 1;
        }
        assertEquals(2, count);
    }

    @Test
    public void other_test() {
        // create a table
        String name = "table6";
        int transaction_id = storage.start_transaction();
        boolean success = create_test_table1(transaction_id, name);
        assertTrue(success);
        LogicalTable table1 = storage.get_table(name, transaction_id);
        storage.drop_table(name, transaction_id);
        storage.commit(transaction_id);

        // create the table again
        transaction_id = storage.start_transaction();
        success = create_test_table1(transaction_id, name);
        assertTrue(success);
        LogicalTable table2 = storage.get_table(name, transaction_id);
        storage.drop_table(name, transaction_id);
        storage.commit(transaction_id);

        // make sure two tables compare by address
        assertFalse(table1 == table2);

        assertTrue(table1.shared_lock());
        assertTrue(table1.shared_lock());
        assertTrue(table1.is_share_locked());
        assertFalse(table1.is_exclusive_locked());
        table1.unlock();
        // shared_lock() is called twice, so two transactions own this lock
        assertTrue(table1.is_share_locked());
        table1.unlock();
        assertFalse(table1.is_share_locked());
        assertFalse(table1.upgrade_lock());
        assertTrue(table1.exclusive_lock());
        assertTrue(table1.is_exclusive_locked());
        assertFalse(table1.exclusive_lock());
        assertTrue(table1.is_exclusive_locked());
        table1.unlock();
        assertFalse(table1.is_exclusive_locked());
        assertTrue(table1.shared_lock());
        assertTrue(table1.shared_lock());
        assertFalse(table1.exclusive_lock());
        // more than one transactions own read lock, can not upgrade
        assertFalse(table1.upgrade_lock());
        table1.unlock();
        assertTrue(table1.upgrade_lock());
        assertTrue(table1.is_exclusive_locked());
        table1.unlock();
        assertFalse(table1.is_exclusive_locked());

    }
}
