package cn.edu.thssdb.schema;

import cn.edu.thssdb.recovery.RecoveryInfo;
import cn.edu.thssdb.recovery.WALManager;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Heap.HeapFile;
import cn.edu.thssdb.utils.Global;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table  {
    ReentrantReadWriteLock lock;

    public class WALBuffer{
        public int pageNum = 0;
        public short pageOffset = 0;
    }

    RowDesc desc;
    private File diskFile;
    public WALManager walManager;
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
    public HeapFile fileHandler;
//  private Integer id;

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
            File indexFile,
            File walFile) throws IOException {
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
        this.walManager = new WALManager(walFile, (HeapFile)( fileHandler));

        redoWalLog();
    }

    private void redoWalLog() {
        try {
            walManager.recover();
            fileHandler.persistIndex();
            walManager.flush();
        }catch (Exception e){
            //TODO
        }
    }

    public Table(
            Integer id,
            String name,
            RowDesc desc,
            File diskFile,
            File indexFile,
            File walFile,
            TableInfo tableInfo
            ) throws IOException{
        this(id, name, desc, diskFile, indexFile, walFile);
        this.tableInfo = tableInfo;
        // TODO
    }

    public ArrayList<Column> getColumns(){
        return desc.getColumns();
    }

    public ArrayList<String> getPrimaryNames(){
        return desc.getPrimaryNames();
    }

    public boolean insertRow(Row row, int txn_id){
        tableInfo.autoIncrement ++;
        tableInfo.count ++;

        WALBuffer buf = new WALBuffer();
        Global.gBufferPool().insertRow(this.tid, row);
        walManager.delete(txn_id, buf.pageNum, buf.pageOffset);
    }

    public boolean insertRow(ArrayList<String> attrNames, ArrayList<Object> values, int txn_id) {
        try {
            Row row = new Row(this.desc, attrNames, values);
            return insertRow(row, txn_id);
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

    public boolean deleteRow(Entry entry, int txn_id) {
        try {
            this.tableInfo.count--;
            WALBuffer buf = new WALBuffer();
            ((HeapFile)this.fileHandler).deleteRow(entry.value, buf);
            walManager.delete(txn_id, buf.pageNum, buf.pageOffset);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    //
    public boolean deleteRow(Row row){
        this.tableInfo.count --;
        return Global.gBufferPool().deleteRow(this.tid, row);
    }

    // warning should never use it !!!
    private Row search(Entry primary_key, int txn_id){
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
//        fileHandler.persistIndex();
        walManager.persist();
    }

    public void discard(){
        Global.gBufferPool().discardPagesOfTables(tid);

    }
}
