package cn.edu.thssdb.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class TableMeta implements Serializable {
    private static final long serialVersionUID = -5809782578272943999L;


    public int tableId;
    public String databaseName;
    public String tableName;
    public int count;
    public int autoIncrement;
    public ArrayList<Column> columnLabel;
    public int byteSize = -1;


    public TableMeta(int tableId,
                     String databaseName,
                     String tableName,
                     int count,
                     int autoIncrement,
                     ArrayList<Column> columnLabel){
        this.databaseName = databaseName;
        this.tableId = tableId;
        this.tableName = tableName;
        this.count = count;
        this.autoIncrement = autoIncrement;
        this.columnLabel = columnLabel;
    }

    public TableMeta(Column[] items,
                     String[] primaryKeys){
        this.columnLabel = (ArrayList<Column>)Arrays.asList(items);
        if(primaryKeys != null){
        }

    }

    public int

    public int getByteSize(){
        if(byteSize < 0){
            byteSize = 0;
            for(Column item : columnLabel){
                byteSize += item.getByteSize();
            }
        }
        return byteSize;
    }

}
