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

//  private ArrayList<Table> dropTables;
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
//    dropTables = new ArrayList<>();
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

  private void recoverCreate(String tableName, RowDesc desc, TableInfo info) throws Exception{
    // TODO
//      tablename2Desc.put(tableName, desc);
//      tablename2Info.put(tableName, info);
    File diskFile = new File(
            Global.synthFilePath(path, databaseName, String.format("%s.db", tableName)));
    Table table = new Table(name2Id.get(tableName), tableName, desc, diskFile, info);


    idTableMap.put(name2Id.get(tableName), table);
  }

  public void drop(String tableName) throws Exception{

    Table table = this.getTable(tableName);
    if(table == null){
      throw new Exception("cannot found table");
    }else{
//      table.getDiskFile().delete(); // 惰性删除,仅在初始化的时候选择删除
//      this.dropTables.add(table);
      Integer id = table.getId();
      idTableMap.remove(id);
      name2Id.remove(tableName);
      tablename2Desc.remove(tableName);
      tablename2Info.remove(tableName);
    }

    // TODO result
  }

  public void sync(boolean isCommit){
    if(!isCommit){
      recover();
    }else {
      persist();
    }
  }


  private void dropAll(){
      for (String tableName : this.tablename2Desc.keySet()) {
        try {
          drop(tableName);
        } catch (Exception e) {
          System.out.println("Warning: has bug in drop database but ignore it");
        }
      }
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

      deleteRedunctant();
    }catch (Exception e){
      //TODO
    }
  }

  private void recover() {
    String root = Global.synthFilePath(path, databaseName);
    if(!newDatabaseDirectory(root)){
      throw new InternalException("not exist");//TODO throw
    }
    String metaFileName = Global.synthFilePath(root, String.format(Global.META_FORMAT, databaseName));
    File metaFile = new File(metaFileName);
    try {
      if(!metaFile.exists())
        metaFile.createNewFile();
      if(metaFile.length() == 0)
        return;//未初始化

      FileInputStream fis = new FileInputStream(metaFileName);
      ObjectInputStream ois = new ObjectInputStream(fis);
      tablename2Desc = (HashMap<String, RowDesc>)ois.readObject();
      tablename2Info = (HashMap<String, TableInfo>) ois.readObject();
      name2Id = (HashMap<String, Integer>) ois.readObject();
      ois.close();
      fis.close();

      idTableMap = new HashMap<>();
      for(String name: tablename2Desc.keySet()){
        RowDesc desc = tablename2Desc.get(name);
        TableInfo info = tablename2Info.get(name);
        recoverCreate(name, desc, info);
      }

      this.deleteRedunctant();
    }catch (Exception e){
      //TODO
      e.printStackTrace();
    }
  }

  /**
   * 作用: 初始化时删除冗余的db
   */
  private void deleteRedunctant() {
    String root = Global.synthFilePath(path, databaseName);
    File dir = new File(root);
    File[] files=dir.listFiles();
    for(int i=0;i<files.length;i++) {
      String fileName = files[i].getName();
      String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
      String name = fileName.substring(0, fileName.lastIndexOf("."));

      if(!suffix.equals(Global.META_SUFFIX) && !tablename2Info.containsKey(name)){
        files[i].delete();
      }
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

  public int tableName2Id(String tableName){
    return this.tableName2Id(tableName);
  }


  public Table getTable(String tableName){
    return this.getTable(name2Id.get(tableName));
  }

  public Table getTable(Integer tableId){
    return this.idTableMap.get(tableId);
  }

  public boolean dropSelf(){
    dropAll();
    sync(true);
    String root = Global.synthFilePath(path, databaseName);
    File dir = new File(root);
    for(File subfile: dir.listFiles()){
      subfile.delete();
    }
    return dir.delete();
  }

}
