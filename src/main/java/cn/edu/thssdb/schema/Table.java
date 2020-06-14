package cn.edu.thssdb.schema;

import cn.edu.thssdb.adapter.HeapTableIterator;
import cn.edu.thssdb.adapter.LogicalTable;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Heap.HeapFile;
import cn.edu.thssdb.utils.Global;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements LogicalTable {
    ReentrantReadWriteLock lock;

    RowDesc desc;
    private File diskFile;
    public int tid;
    public String databaseName;
    public String tableName;
    private TableInfo tableInfo;
//    public int count = 0;
//    public int autoIncrement = 0;
    //  private String databaseName;
//  public String tableName;
    public HashMap<String, Integer> columnIndex; //通过column的名称查询其所在列
//  public ArrayList<Column> columnLabel;

    //  public BPlusTree<Entry, Row> index;
    private FileHandler fileHandler;
//  private Integer id;
    private int lock_state;
    public int txnId = 0;

    private int primaryIndex;

    public RowDesc getTableMeta(){
        return desc;
    }


    public File getDiskFile() {
        return diskFile;
    }

    public Integer getId() {
        return tid;
    }

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    private Table(
            Integer id,
            String name,
            RowDesc desc,
            File diskFile,
            File indexFile) throws IOException {
        // TODO
        this.tid = id;
        this.lock = new ReentrantReadWriteLock();
        this.desc = desc;
        this.columnIndex = new HashMap<>();
        for(int i = 0; i < desc.getColumnSize(); i ++){
            columnIndex.put(desc.get(i).getName(), i);
        }
        this.fileHandler = new HeapFile(id, diskFile, indexFile, desc);
        this.tableName = name;
        this.diskFile = diskFile;
        this.tableInfo = new TableInfo(0, 0);
    }

    public Table(
            Integer id,
            String name,
            RowDesc desc,
            File diskFile,
            File indexFile,
            TableInfo tableInfo
            ) throws IOException{
        this(id, name, desc, diskFile, indexFile);
        this.tableInfo = tableInfo;
        // TODO
    }

    public ArrayList<Column> getColumns(){
        return desc.getColumns();
    }

    public ArrayList<String> getPrimaryNames(){
        return desc.getPrimaryNames();
    }

    public boolean insertRow(Row row){
        tableInfo.autoIncrement ++;
        tableInfo.count ++;
        return Global.gBufferPool().insertRow(this.tid, row);
    }

    public boolean insertRow(ArrayList<String> attrNames, ArrayList<Object> values) {
        try {
            Row row = new Row(this.desc, attrNames, values);
            return insertRow(row);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public FileIterator getIterator(){
        FileIterator iterator = fileHandler.iterator();
        iterator.open();
        return iterator;
    }


    //
    public boolean deleteRow(Row row){
        this.tableInfo.count --;
        return Global.gBufferPool().deleteRow(this.tid, row);
    }

    // warning should never use it !!!
    private Row search(Entry primary_key){
        primaryIndex = desc.getPrimaryIndex().get(0);
        FileIterator iterator = getIterator();
        while(iterator.hasNext()){
            Row row = iterator.next();
            if(row.getEntries().get(primaryIndex).equals(primary_key)){
                return row;
            }
        }
        return null;
    }

    public void flush() throws IOException{
        Global.gBufferPool().flushPagesOfTable(tid);
        fileHandler.persistIndex();
        
    }

    public void discard() throws  IOException{
        Global.gBufferPool().discardPagesOfTables(tid);
        ((HeapFile)fileHandler).recoverIndex();
    }

    @Override
    public boolean insert(Row row) {
        return this.insertRow(row);
    }

    @Override
    public boolean delete(Entry entry) {
        try{
            // 通过iterator遍历,来完成删除对应的操作
            boolean success = false;
            FileIterator iter = this.getIterator();
            ArrayList<Row> rows = new ArrayList<>();
            while(iter.hasNext()){
                Row row = iter.next();
                if(row.matchValue(this.getTableMeta().getPrimaryNames().get(0), entry.value)){
                    //table调用index在文件中删去row
                    success = this.deleteRow(row);
                }
            }
            iter.close();
            return success;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean shared_lock() {
        if (this.lock_state >= 0) {
            this.lock_state += 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean exclusive_lock() {
        if (this.lock_state == 0) {
            this.lock_state = -1;
            return true;
        }
        return false;
    }

    @Override
    public boolean is_share_locked() {
        return lock_state > 0;
    }

    @Override
    public boolean is_exclusive_locked() {
        return lock_state == -1;
    }

    @Override
    public boolean upgrade_lock() {
        // there is only one shared lock
        // need to assume the caller is the owner of the shared lock
        if (lock_state == 1) {
            lock_state = -1;
            return true;
        }
        return false;
    }

    @Override
    public void unlock(boolean isCommit) {
        if (lock_state > 0) {
            lock_state -= 1;
        }
        else {
            lock_state = 0;
            try {
                if (isCommit)
                    flush();
                else
                    discard();
            }catch(Exception e){

            }
        }
    }

    @Override
    public ArrayList<Column> get_columns() {
        return this.getTableMeta().getColumns();
    }

    @Override
    public String get_name() {
        return this.tableName;
    }

    @Override
    public Iterator<Row> iterator() {
        return new HeapTableIterator(this.getIterator());
    }
}
