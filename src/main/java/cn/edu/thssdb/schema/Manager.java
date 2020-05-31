package cn.edu.thssdb.schema;

import cn.edu.thssdb.utils.Global;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Manager {
  private HashMap<String, Database> databases;
  private Database currentDatabase = null;
  private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private String path;
  private String metaPath;

  public static Manager getInstance() {
    return Manager.ManagerHolder.INSTANCE;
  }

  private Manager(){
    this.path = Global.DEFAULT_SQLPATH;
    this.metaPath = Global.DEFAULT_METAPATH;
    databases = new HashMap<>();
    Global.createDir(this.path);
  }
  public void changePath(String sqlPath, String metaPath) {
    this.path = sqlPath;
    this.metaPath = metaPath;
    Global.createDir(this.path);
  }

  public void createDatabase(String databaseName) throws Exception{
    if(databases.containsKey(databaseName))
      throw new Exception("database already exist");
    createDatabaseIfNotExists(databaseName);
  }

  private void createDatabaseIfNotExists(String databaseName) {
    // TODO
    try {
      lock.writeLock().lock();
      if (databases.containsKey(databaseName))
        return;
      Database database = new Database(
              path,
              databaseName);
      databases.put(databaseName, database);
    }finally {
      lock.writeLock().unlock();
    }
  }

  private void persist(){
    if(databases.size() > 0){
      try {
        File file = new File(metaPath);
        FileWriter fileWriter = new FileWriter(file, false);
        fileWriter.write(path);
        fileWriter.write(System.lineSeparator());
        for(String name : databases.keySet()) {
          fileWriter.write(name);
          fileWriter.write(System.lineSeparator());
        }
        fileWriter.close();
      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }

  public void exit(){
    try{
      lock.writeLock().lock();;
      persist();
      for(Database database: databases.values()){
        database.quit();
      }
  }finally {
      lock.writeLock().unlock();
    }
  }

  public void recover(){
    File file = new File(metaPath);
    try{
      if(file.exists()){
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        path = bufferedReader.readLine();
        String databaseName;
        while((databaseName = bufferedReader.readLine()) != null){
          Database database = new Database(
                  path,
                  databaseName);
          databases.put(databaseName, database);
        }
        bufferedReader.close();
        fileReader.close();
      }
    }catch(Exception e){

    }
  }

  public void useDatabase(String databaseName){
    try{
      lock.readLock().lock();
      if(databases.containsKey(databaseName)) {
        Database database = databases.get(databaseName);
        currentDatabase = database;
      }else{
        throw new InternalError("");//TODO exception
      }
    }finally {
      lock.readLock().unlock();
    }
  }



  private void deleteDatabase(String databaseName) {
    // TODO
    try{
      lock.writeLock().lock();
      if(databases.containsKey(databaseName)){
        databases.get(databaseName).dropAll();
        databases.remove(databaseName);
      }
    }finally {
      lock.writeLock().unlock();
    }
  }

//  public Database switchDatabase(String databaseName) {
//    return useDatabase(databaseName);
//  }

//  public String getDefaultDatabaseName(){
//    return databases.keySet().iterator().next();
//  }

  public Database getCurrentDatabase(){
    return  currentDatabase;
  }

  private static class ManagerHolder {
    private static final Manager INSTANCE = new Manager();
    private ManagerHolder() {

    }
    public static Manager getInstance(){
      return INSTANCE;
    }
  }
}
