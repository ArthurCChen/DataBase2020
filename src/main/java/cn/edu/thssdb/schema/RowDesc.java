package cn.edu.thssdb.schema;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class RowDesc implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Column> columns;
    private ArrayList<String> primaryKeys;
    private ArrayList<Integer> primaryIndex;

    public ArrayList<String> getAttrNames(){
        ArrayList<String> attrs = new ArrayList<>();
        for(Column column: columns){
            attrs.add(column.getName());
        }
        return attrs;
    }


    public RowDesc(ArrayList<Column> columns, ArrayList<String> primaryKeys){
        this.columns = new ArrayList<>(columns);
        if (primaryKeys != null && primaryKeys.size() != 0){
            this.primaryIndex = new ArrayList<Integer>();
            this.primaryKeys = new ArrayList<>(primaryKeys);
            for(int i = 0; i < columns.size(); i ++){
                if(primaryKeys.contains(columns.get(i).getName())){
                    this.primaryIndex.add(i);
                    this.columns.get(i).setPrimary(Column.PRIMARY);
                }
            }
        }else {
            this.primaryKeys = new ArrayList<String>();
            this.primaryIndex = new ArrayList<Integer>();
        }
    }

    public RowDesc(ArrayList<Column> columns, String primaryKey){
        this(columns, new ArrayList<String>(Arrays.asList(primaryKey)));
    }

    public Iterator<Column> iterator() {return columns.iterator();}

    public int getColumnSize(){
        return columns.size();
    }

    public Column get(int i){
        return columns.get(i);
    }

    // -1 means no such index
    public int columnName2Index(String name){
        for(int i = 0; i < columns.size(); i ++){
            if(columns.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public int columnName2Index(String tableName, String columnName){
        for(int i = 0; i < columns.size(); i ++){
            if(columns.get(i).isName(tableName, columnName))
                return i;
        }
        return -1;
    }

    public void updateTableName(String tableName){
        for(int i = 0; i < columns.size(); i ++){
            columns.get(i).setTableName(tableName);
        }
    }

    public int getByteSize(){
        int bytes = 0;
        for(Column column: columns){
            bytes += column.getByteSize();
        }
        return bytes;
    }

    public ArrayList<Integer> getPrimaryIndex() {
        return primaryIndex;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(Column column: columns){
            stringBuilder.append(column.toString());
            stringBuilder.append(",");
        }
        stringBuilder.append(" PRIMARY KEY(");
        for(int i = 0; i < primaryKeys.size(); i ++){
            stringBuilder.append(primaryKeys.get(i));
            if(i < primaryKeys.size() - 1){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();

    }
}
