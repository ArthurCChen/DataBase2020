package cn.edu.thssdb.utils;

import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.storage.BufferPool;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.type.ColumnType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Global {
  public static int fanout = 129;

  public static int SUCCESS_CODE = 0;
  public static int FAILURE_CODE = -1;

  public static String DEFAULT_SERVER_HOST = "127.0.0.1";
  public static int DEFAULT_SERVER_PORT = 6667;

  public static final String ROLLBACK = "rollback;";
  public static final String START_TRANSACTION = "start transaction;";
  public static final String COMMIT = "commit;";

  public static String CLI_PREFIX = "ThssDB>";
  public static final String SHOW_TIME = "show time;";
  public static final String QUIT = "quit;";
  public static final String SHOW_HELP = "help;";

  public static final String S_URL_INTERNAL = "jdbc:default:connection";
  public static final String FILE_SEPARATOR = File.separator;
  public static final String DATA_FORMAT = "%s.db";
  public static final String META_FORMAT = "%s.meta";
  public static  final String INDEX_FORMAT = "%s.myi";
  public static final String META_SUFFIX = "meta";
  public static final String SCRIPT_FORMAT = "%s.script";
  public static final String LOG_FORMAT = "%s.log";

  public static final String DEFAULT_SQLPATH = "data/";
  public static final String DEFAULT_METAPATH = "catlog.meta";

  public static final String NULL_VALUE_DISPLAY = "NULL";
  //--------------------------------------------------------
  //--------For Storage usage-------------------------------
  //--------------------------------------------------------
  public static final int pageSize = 4096;
  public static final int bufferChunkSize = 50;

  //用于转换Object为数组类型
  //https://www.cnblogs.com/xingmangdieyi110/p/11676553.html
  public static <T> List<T> castList(Object obj, Class<T> clazz) throws ClassCastException
  {
    List<T> result = new ArrayList<T>();
    if(obj instanceof List<?>)
    {
      for (Object o : (List<?>) obj)
      {
        result.add(clazz.cast(o));
      }
      return result;
    }
    return null;
  }

  public static Comparable castValue(Comparable value, ColumnType type){
    if(value == null)
      return null;

    switch(type){
      case STRING:
        return value;
      case LONG:
        return ((Number) value).longValue();
      case FLOAT:
        return ((Number)value).floatValue();
      case DOUBLE:
        return ((Number)value).doubleValue();
      case INT:
        return ((Number)value).intValue();
    }
    throw new InternalError("unvalid type");
  }

  public static FileHandler getFileFromPid(PageId pid){
    //TODO: 补充
    return Manager.getInstance().getCurrentDatabase().getTable(pid.getTableId()).getFileHandler();
  }

//  public static Table getTableFromTid(String tid){
//    //TODO:
//    return null;
//  }

  public static Table getTableFromTid(Integer tid){
    //TODO:
    Manager manager = Manager.getInstance();
    //TODO: check in Database first
    return manager.getCurrentDatabase().getTable(tid);
  }

  public static BufferPool gBufferPool(){
    //TODO
    BufferPool gBufferPool = BufferPoolHolder.getInstance();
    return gBufferPool;
  }

  public static String synthFilePath(String ... paths){
    Path path = path = Paths.get(paths[0]);;
    for (int i = 1; i < paths.length; i ++){
      path = path.resolve(paths[i]);
    }
    return path.toString();
  }

  public static void createDir(String path){
    File file = new File(path);
    if(!file.exists()){
      file.mkdir();
    }
  }

  private static class BufferPoolHolder {
    private static final BufferPool INSTANCE = new BufferPool();
    private BufferPoolHolder() {

    }
    public static BufferPool getInstance(){
      return INSTANCE;
    }

  }

  static public ArrayList<String> getPrimaryKeysFromColumns(ArrayList<Column> columns){
    ArrayList<String> primaryKeys = new ArrayList<>();
    for(Column column:columns){
      if(column.getPrimary())
        primaryKeys.add(column.getName());
    }
    return primaryKeys;
  }
}
