import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.type.ColumnType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertThrows;

public class TableTest {
    Manager manager;
    @Before
    public void createTable(){
        Manager manager = Manager.getInstance();
        manager.recover();
        try{
            manager.createDatabase("test");
        }catch( Exception e) {
//            return;//已经初始化过了
        }
        manager.useDatabase("test");
        Column[] c = {
                new Column("id", ColumnType.INT, true, 0, "test"),
                new Column("name", ColumnType.STRING, true, 10, "test"),
                new Column("grade", ColumnType.DOUBLE, false, 0, "test")
        };
        ArrayList<Column> columns = new ArrayList<Column>(Arrays.asList(c));
        ArrayList<String> primary = new ArrayList<String>(Arrays.asList("id"));
        Database db = manager.getCurrentDatabase();
        try {
            db.create("test", columns, primary);
        }catch (Exception e){
            return;
        }
        Table table = db.getTable("test");
        ArrayList<String> attrs = new ArrayList<String>(Arrays.asList("id", "name", "grade"));
        ArrayList<Object> val1 = new ArrayList<>(Arrays.asList(1, "bad0", 4.0));
        ArrayList<Object> val2 = new ArrayList<>(Arrays.asList(2, "bad1", 3.6));
        ArrayList<Object> val3 = new ArrayList<>(Arrays.asList(3, "bad2", 3.3));
        ArrayList<Object> val4 = new ArrayList<>(Arrays.asList(4, "bad3", 0.0));

        table.insertRow( attrs ,
                val1);
        table.insertRow(attrs, val2);
        table.insertRow(attrs, val3);
        table.insertRow(attrs, val4);
        //相当于保存

        manager.exit();
    }

    @Ignore
    public void dropTable(){
        manager = Manager.getInstance();
        manager.recover();
        manager.useDatabase("test");
        assertThrows(Exception.class, ()->manager.getCurrentDatabase().drop("test") );
        manager.exit();
    }

    @Test
    public void Search(){
        manager = Manager.getInstance();
        manager.recover();
        manager.useDatabase("test");
        Table table = manager.getCurrentDatabase().getTable("test");
        FileIterator iter = table.getIterator();
        ArrayList<Row> rows = new ArrayList<>();
        while(iter.hasNext()){
            Row row = iter.next();
            if(row.matchValue("name", "bad1")){
                rows.add(row);
            }
        }
        System.out.println(rows);
        iter.close();

        manager.exit();
    }
    @Test
    public void nullVal(){
        manager = Manager.getInstance();
        manager.recover();
        manager.useDatabase("test");
        Table table = manager.getCurrentDatabase().getTable("test");
        ArrayList<String> attrs = new ArrayList<String>(Arrays.asList("id", "name"));
        ArrayList<Object> val = new ArrayList<>(Arrays.asList(6, "chuchong"));
        table.insertRow(attrs, val);

        FileIterator iter = table.getIterator();
        ArrayList<Row> rows = new ArrayList<>();
        while(iter.hasNext()){
            Row row = iter.next();
            rows.add(row);
        }
        System.out.println(rows);
        iter.close();

        manager.exit();
    }

    @Test
    public void deleteVal(){
        manager = Manager.getInstance();
        manager.recover();
        manager.useDatabase("test");
        Table table = manager.getCurrentDatabase().getTable("test");

        FileIterator iter = table.getIterator();
        while(iter.hasNext()){
            Row row = iter.next();
            if(row.matchValue("id", 6)){
                table.deleteRow(row);
            }
        }
        iter.close();

        manager.exit();
    }
}
