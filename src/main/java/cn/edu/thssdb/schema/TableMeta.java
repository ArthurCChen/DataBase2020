package cn.edu.thssdb.schema;

import java.io.Serializable;
import java.util.ArrayList;

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

    public int getSize(){
        if(byteSize < 0){
            byteSize = 0;
            for(Column item : columnLabel){
                byteSize += item.
            }
        }
        return byteSize;
    }

}
