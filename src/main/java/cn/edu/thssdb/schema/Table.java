package cn.edu.thssdb.schema;

import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Heap.HeapFile;
import cn.edu.thssdb.utils.Global;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table  {
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
            File diskFile){
        // TODO
        this.tid = id;
        this.lock = new ReentrantReadWriteLock();
        this.desc = desc;
        this.columnIndex = new HashMap<>();
        for(int i = 0; i < desc.getColumnSize(); i ++){
            columnIndex.put(desc.get(i).getName(), i);
        }
        this.fileHandler = new HeapFile(id, diskFile, desc);
        this.tableName = name;
        this.diskFile = diskFile;
        this.tableInfo = new TableInfo(0, 0);
    }

    public Table(
            Integer id,
            String name,
            RowDesc desc,
            File diskFile,
            TableInfo tableInfo
            ){
        this(id, name, desc, diskFile);
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

}
