package cn.edu.thssdb.adapter;

import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.storage.FileIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;

public class HeapTable implements LogicalTable {
    Table table ;
    public HeapTable(Table table) {
        this.table = table;
    }

    @Override
    public boolean insert(Row row) {
        return table.insertRow(row);
    }

    @Override
    public boolean delete(Entry entry) {
        try{

            // 通过iterator遍历,来完成删除对应的操作
            boolean success = false;
            FileIterator iter = table.getIterator();
            ArrayList<Row> rows = new ArrayList<>();
            while(iter.hasNext()){
                Row row = iter.next();
                if(row.matchValue(table.getTableMeta().getPrimaryNames().get(0), entry.value)){
                    //table调用index在文件中删去row
                    success = table.deleteRow(row);
                }
            }
            iter.close();
            return success;
        }catch (Exception e){
            return false;
        }
    }

    //TODO
    @Override
    public boolean shared_lock() {
        return false;
    }

    //TODO
    @Override
    public boolean exclusive_lock() {
        return false;
    }

    //TODO
    @Override
    public boolean is_share_locked() {
        return false;
    }

    //TODO
    @Override
    public boolean is_exclusive_locked() {
        return false;
    }

    //TODO
    @Override
    public boolean upgrade_lock() {
        return false;
    }

    //TODO
    @Override
    public void unlock() {

    }

    @Override
    public ArrayList<Column> get_columns() {
        return table.getTableMeta().getColumns();
    }

    @Override
    public String get_name() {
        return table.tableName;
    }


    @Override
    public Iterator<Row> iterator() {
        return new HeapTableIterator(table.getIterator());
    }

    /**
     * not supported
     * @deprecated
     * @return
     */
    @Override
    public Spliterator<Row> spliterator(){
        assert(false);
        return null;
    }
}
