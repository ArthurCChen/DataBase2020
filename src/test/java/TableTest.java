import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Global;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class TableTest {
    @Test
    public void insertTable(){

        Manager manager = Manager.getInstance();
        try{
            manager.createDatabase("test");
        }catch( Exception e){

        }
        manager.useDatabase("test");
        Column[] c = {
                new Column("id", ColumnType.INT, true, 0, "test"),
                new Column("name", ColumnType.STRING, true, 10, "test")
        };
        ArrayList<Column> columns = new ArrayList<Column>(Arrays.asList(c));
        ArrayList<String> names = new ArrayList<String>(Arrays.asList("id", "name"));
        Database db = manager.getCurrentDatabase();
        db.create("test", columns, names);

        Table table = db.getTable("test");
        ArrayList<Object> val = new ArrayList<>(Arrays.asList(1, "bad"));

        table.insertRow( new ArrayList<String>(Arrays.asList("id", "name")),
                val);

        //相当于保存
        Global.gBufferPool().flushAllPages();


    }
}
