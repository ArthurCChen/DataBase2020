package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.FileNotExistException;
import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.index.BPlusTreeIterator;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.Heap.HeapFile;
import cn.edu.thssdb.utils.Global;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Iterable<Row> {
    ReentrantReadWriteLock lock;

    RowDesc meta;
    private File diskFile;
    public int tid;
    public String databaseName;
    public String tableName;
    public int count = 0;
    public int autoIncrement = 0;
    //  private String databaseName;
//  public String tableName;
    public HashMap<String, Integer> columnIndex; //通过column的名称查询其所在列
//  public ArrayList<Column> columnLabel;

    //  public BPlusTree<Entry, Row> index;
    private FileHandler fileHandler;
//  private Integer id;

    private int primaryIndex;

    public RowDesc getTableMeta(){
        return meta;
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

    public Table(
            Integer id,
            String name,
            RowDesc meta,
            File diskFile){
        // TODO
        this.lock = new ReentrantReadWriteLock();
        this.meta = meta;
        this.columnIndex = new HashMap<>();
        for(int i = 0; i < meta.getColumnSize(); i ++){
            columnIndex.put(meta.get(i).getName(), i);
        }
        this.fileHandler = new HeapFile(id, diskFile, meta);
        this.tableName = name;
        this.diskFile = diskFile;
        count = 0;
    }

    public void insertRow(Row row){
        try{
            Global.gBufferPool().insertRow(this.tid, row);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertRow(ArrayList<Object> values){

    }


}
