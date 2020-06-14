package cn.edu.thssdb.adapter;

import cn.edu.thssdb.exception.FlushIOException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.storage.FileIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;

public class HeapTable implements LogicalTable {

    Table table ;
//    private int lock_state;
    static private HashMap<Integer, Integer> lockStatasMap = null;

    public HeapTable(Table table)
    {
        this.table = table;
        if(lockStatasMap == null)
            lockStatasMap = new HashMap<>();
        if(!lockStatasMap.containsKey(table.tid)) {
            lockStatasMap.put(table.tid, 0);
        }
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
        int lock_state = lockStatasMap.get(table.tid);
        if (lock_state >= 0) {
            lock_state += 1;
            lockStatasMap.replace(table.tid, lock_state);
            return true;
        }
        return false;
    }

    //TODO
    @Override
    public boolean exclusive_lock() {
        int lock_state = lockStatasMap.get(table.tid);
        if (lock_state == 0) {
            lock_state = -1;
            lockStatasMap.replace(table.tid, lock_state);
            return true;
        }
        return false;
    }

    //TODO
    @Override
    public boolean is_share_locked() {
        int lock_state = lockStatasMap.get(table.tid);
        return lock_state > 0;
    }

    //TODO
    @Override
    public boolean is_exclusive_locked() {
        int lock_state = lockStatasMap.get(table.tid);
        return lock_state == -1;
    }

    //TODO
    @Override
    public boolean upgrade_lock() {
        int lock_state = lockStatasMap.get(table.tid);
        if (lock_state == 1) {
            lock_state = -1;
            lockStatasMap.replace(table.tid, lock_state);
            return true;
        }
        return false;
    }

    //TODO
    @Override
    public void unlock( boolean isCommit) {
        int lock_state = lockStatasMap.get(table.tid);
        if (lock_state > 0) {
            lock_state -= 1;
        }
        else {
            lock_state = 0;
            if(isCommit){
                try {
                    table.flush();
                }catch(IOException e){
                    throw new FlushIOException();
                }
            }else {
                try {
                    table.discard();
                } catch(IOException e){
                    throw new FlushIOException();
                }
            }
        }
        lockStatasMap.replace(table.tid, lock_state);
    }

    @Override
    public ArrayList<Column> get_columns()
    {
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
