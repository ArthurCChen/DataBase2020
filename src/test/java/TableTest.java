import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Global;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
                new Column("name", ColumnType.STRING, true, 10, "test")
        };
        ArrayList<Column> columns = new ArrayList<Column>(Arrays.asList(c));
        ArrayList<String> names = new ArrayList<String>(Arrays.asList("id", "name"));
        Database db = manager.getCurrentDatabase();
        try {
            db.create("test", columns, names);
        }catch (Exception e){
            return;
        }
        Table table = db.getTable("test");
        ArrayList<String> attrs = new ArrayList<String>(Arrays.asList("id", "name"));
        ArrayList<Object> val1 = new ArrayList<>(Arrays.asList(1, "bad0"));
        ArrayList<Object> val2 = new ArrayList<>(Arrays.asList(2, "bad1"));
        ArrayList<Object> val3 = new ArrayList<>(Arrays.asList(3, "bad2"));
        ArrayList<Object> val4 = new ArrayList<>(Arrays.asList(4, "bad3"));

        table.insertRow( attrs ,
                val1);
        table.insertRow(attrs, val2);
        table.insertRow(attrs, val3);
        table.insertRow(attrs, val4);
        //相当于保存
        Global.gBufferPool().flushAllPages();
        manager.exit();
    }

    @Test
    public void dropTable(){
        manager = Manager.getInstance();
        manager.recover();
        manager.useDatabase("test");
        manager.getCurrentDatabase().drop("test");
        manager.exit();
    }
}
