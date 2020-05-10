package cn.edu.thssdb.schema;

import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.utils.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

  private String databaseName;
  private HashMap<String, TableMeta> tablename2meta;
  ReentrantReadWriteLock lock;
  private HashMap<Integer, Table> idTableMap;
  private static int gId = 0;
  public String path;

  public Database(String path, String name) {
    this.databaseName = name;
    this.path = path;
    this.tablename2meta = new HashMap<>();
    this.idTableMap = new HashMap<>();
    this.lock = new ReentrantReadWriteLock();
    recover();
  }

  private Boolean newDatabaseDirectory(String root){
    File rootFile = new File(root);
    return rootFile.exists() || rootFile.mkdirs();
  }

  private void persist() {
    String root = Global.synthFilePath(path, databaseName);
    if(!newDatabaseDirectory(root)){
      return;//TODO throw
    }
    String databaseScriptFile = Global.synthFilePath(root, String.format(Global.META_FORMAT, databaseName));
    try {
      FileOutputStream fos = new FileOutputStream(databaseScriptFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(tablename2meta);
      oos.close();
      fos.close();
    }catch (Exception e){
      //TODO
    }
  }

  public void create(String tableName, Column[] columns) {
    // TODO
    if(getTable(tableName) != null){
      return; //TODO throw error

    }else{
      File diskFile = new File(
              Global.synthFilePath(path, databaseName, String.format("%s.db", tableName)));
      try{
        diskFile.createNewFile();
      }catch (Exception e){
        e.printStackTrace();
      }

    this.gId ++;
    TableMeta meta = new TableMeta(gId, databaseName, tableName, 0, 0, new ArrayList<>(Arrays.asList(columns)));
    Table table = new Table(meta, diskFile);
    tablename2meta.put(tableName, meta);
    idTableMap.put(gId, table);
    //TODO return result
    }
  }

  public void drop(String tableName) {

    Table table = this.getTable(tableName);
    if(table == null){

    }else{
      table.getDiskFile().delete();
      Integer id = table.getId();
      idTableMap.remove(id);
      tablename2meta.remove(tableName);
    }

    // TODO result
  }

  public FileHandler getFileHandler(String tableName){
    return getTable(tableName).getFile();
  }

//  public String select(QueryTable[] queryTables) {
//    // TODO
//    QueryResult queryResult = new QueryResult(queryTables);
//    return null;
//  }

  private void recover() {
    String root = Global.synthFilePath(path, databaseName);
    if(!newDatabaseDirectory(root)){
      return;//TODO throw
    }
    String databaseScriptFile = Global.synthFilePath(root, String.format(Global.META_FORMAT, databaseName));
    try {
      FileInputStream fis = new FileInputStream(databaseScriptFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      tablename2meta = (HashMap<String, TableMeta>)ois.readObject();
      ois.close();
      fis.close();
    }catch (Exception e){
      //TODO
    }
  }

  private void recoverFromScript(){
    //TODO 从记录的sql script建立database的column项
  }

  public void quit() {
    // TODO
    try {
      lock.writeLock().lock();
      persist();
      Global.gBufferPool().flushAllPages();//因为与DM操作的接口只有BufferPool
    }finally {
      lock.writeLock().unlock();
    }
  }

  public Table getTable(String tableName){
    return this.getTable(this.tablename2meta.get(tableName).tableId);
  }

  public Table getTable(Integer tableId){
    return this.idTableMap.get(tableId);
  }

}
