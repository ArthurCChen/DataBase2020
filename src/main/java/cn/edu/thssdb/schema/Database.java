package cn.edu.thssdb.schema;

import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.utils.Global;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

  private String databaseName;
  private HashMap<String, Integer> name2Id;
  private HashMap<String, RowDesc> tablename2Desc;
  private HashMap<String, TableInfo>tablename2Info;
  ReentrantReadWriteLock lock;
  private HashMap<Integer, Table> idTableMap;
  private static int gId = 0;
  public String path;

  public Database(String path, String name) {
    this.databaseName = name;
    this.path = path;
    this.name2Id = new HashMap<>();
    this.tablename2Desc = new HashMap<>();
    this.tablename2Info = new HashMap<>();
    this.idTableMap = new HashMap<>();
    this.lock = new ReentrantReadWriteLock();
    recover();
  }

  private Boolean newDatabaseDirectory(String root){
    File rootFile = new File(root);
    return rootFile.exists() || rootFile.mkdirs();
  }


  public void create(String tableName, ArrayList<Column> columns) throws   Exception{
    ArrayList<String> primaryNames = new ArrayList<>();
    for (int i = 0; i < columns.size(); i ++){
      if(columns.get(i).getPrimary()){
        primaryNames.add(columns.get(i).getName());
      }
    }
      create(tableName, columns, primaryNames);
  }

  public void create(String tableName, RowDesc desc, TableInfo info) throws Exception{
    create(tableName, desc.getColumns(), desc.getPrimaryNames(), info);
  }

  // 默认新增,添加为(0,0)
  public void create(String tableName, ArrayList<Column> columns, ArrayList<String> primaryNames) throws Exception{
    TableInfo tableInfo = new TableInfo();
    create(tableName, columns, primaryNames, tableInfo);
  }

  public void create(String tableName, ArrayList<Column> columns, ArrayList<String> primaryNames, TableInfo info) throws Exception{
    // TODO
    if(getTable(tableName) != null){
      throw new Exception("already exist");

    }else{
      File diskFile = new File(
              Global.synthFilePath(path, databaseName, String.format(Global.DATA_FORMAT, tableName)));
      File indexFile = new File(
              Global.synthFilePath(path, databaseName, String.format(Global.INDEX_FORMATA, tableName)));
      try{
        diskFile.createNewFile();
        indexFile.createNewFile();
      }catch (Exception e){
        e.printStackTrace();
      }

    this.gId ++;
      RowDesc desc = new RowDesc(columns, primaryNames);
      Table table = new Table(gId, tableName, desc, diskFile, indexFile, info);
    tablename2Desc.put(tableName, desc);
    tablename2Info.put(tableName, info);
    name2Id.put(tableName, gId);

    idTableMap.put(gId, table);
    //TODO return result
    }
  }

  public void drop(String tableName) throws Exception{

    Table table = this.getTable(tableName);
    if(table == null){
      throw new Exception("cannot found table");
    }else{
      table.getDiskFile().delete();

      Integer id = table.getId();
      idTableMap.remove(id);
      name2Id.remove(tableName);
      tablename2Desc.remove(tableName);
      tablename2Info.remove(tableName);
    }

    // TODO result
  }

  public void dropAll() throws Exception{
    for(String tableName : this.tablename2Desc.keySet())
      drop(tableName);
  }

  public FileHandler getFileHandler(String tableName){
    return getTable(tableName).getFileHandler();
  }

//  public String select(QueryTable[] queryTables) {
//    // TODO
//    QueryResult queryResult = new QueryResult(queryTables);
//    return null;
//  }

  private void persist() {
    String root = Global.synthFilePath(path, databaseName);
    if(!newDatabaseDirectory(root)){
      return;//TODO throw
    }
    String databaseScriptFile = Global.synthFilePath(root, String.format(Global.META_FORMAT, databaseName));
    try {
      FileOutputStream fos = new FileOutputStream(databaseScriptFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(tablename2Desc);
      oos.writeObject(tablename2Info);
      oos.writeObject(name2Id);
      oos.close();
      fos.close();
    }catch (Exception e){
      //TODO
    }
  }

  private void recover() {
    String root = Global.synthFilePath(path, databaseName);
    if(!newDatabaseDirectory(root)){
      throw new InternalException("not exist");//TODO throw
    }
    String databaseScriptFile = Global.synthFilePath(root, String.format(Global.META_FORMAT, databaseName));
    try {
      FileInputStream fis = new FileInputStream(databaseScriptFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      tablename2Desc = (HashMap<String, RowDesc>)ois.readObject();
      tablename2Info = (HashMap<String, TableInfo>) ois.readObject();
      name2Id = (HashMap<String, Integer>) ois.readObject();
      ois.close();
      fis.close();

      for(String name: tablename2Desc.keySet()){
        RowDesc desc = tablename2Desc.get(name);
        TableInfo info = tablename2Info.get(name);
        create(name, desc, info);
      }
    }catch (Exception e){
      //TODO
    }
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
    return this.getTable(name2Id.get(tableName));
  }

  public Table getTable(Integer tableId){
    return this.idTableMap.get(tableId);
  }

}
