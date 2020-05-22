package cn.edu.thssdb.schema;

import cn.edu.thssdb.exception.FileNotExistException;
import cn.edu.thssdb.exception.KeyNotExistException;
import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.index.BPlusTreeIterator;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.utils.Global;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Iterable<Row> {
  ReentrantReadWriteLock lock;
  private TableMeta meta;
//  private String databaseName;
//  public String tableName;
  public HashMap<String, Integer> columnIndex; //通过column的名称查询其所在列
//  public ArrayList<Column> columnLabel;
  private File diskFile;
  public BPlusTree<Entry, Row> index;
  private FileHandler file;
//  private Integer id;

  private int primaryIndex;

  public TableMeta getTableMeta(){
    return meta;
  }


  public File getDiskFile() {
    return diskFile;
  }

  public Integer getId() {
    return meta.tableId;
  }

  public FileHandler getFile() {
    return file;
  }

  public Table(
              TableMeta tableMeta,
               File diskFile){
    // TODO
    this.lock = new ReentrantReadWriteLock();
    this.meta = tableMeta;
    this.diskFile = diskFile;
    this.columnIndex = new HashMap<>();
    for(int i = 0; i < meta.columnLabel.size(); i ++){
      columnIndex.put(meta.columnLabel.get(i).getName(), i);
      if(meta.columnLabel.get(i).getPrimary() == Column.PRIMARY) {
        primaryIndex = i;
        break;
      }
    }
    try {
      recover();
      }catch (FileNotExistException e){
        //TODO 创建文件
      }
    }



  public Row findRowByPrimaryKey(Entry primaryKey) throws RuntimeException {
    if (!this.index.contains(primaryKey)) {
      // 主键不存在, 返回KeyNotExistException.
      throw new KeyNotExistException();
    } else {
      return this.index.get(primaryKey);
    }
  }


  private void recover(){
    // TODO
    String filename = Global.synthFilePath(meta.databaseName, String.format(Global.DATA_FORMAT, meta.tableName));
    File file = new File(filename);
    if(!file.exists())
      throw new InternalException(filename);//TODO exception

    ArrayList<Row> rows;
    try{
      rows = deserialize(filename);
    } catch (Exception e){
      throw new InternalException("failed to deserialize.");
    }


    for(Row row: rows){
      ArrayList<Entry> entries = row.getEntries();
      for(int i = 0; i < meta.columnLabel.size(); i ++){
        Entry entry = entries.get(i);
        if(i == primaryIndex){
          index.put(entry, row);
        }
      }
    }
  }

  // columnNames  一维排列的column所属名称
  // values       上面对应的值
  public void insert(String[] values, String[] columnNames) {
    // TODO

    Entry[] entries = new Entry[meta.columnLabel.size()];
    for (int i = 0 ; i < entries.length; i ++) {
      String columnName = columnNames[i];
      int columnIndex = this.columnIndex.get(columnName);
      Column column = meta.columnLabel.get(columnIndex);
      Comparable castValue = Global.castValue(values[i], column.getType());
      entries[i] = new Entry(castValue);
    }

    Row row = new Row(entries);
    this.lock.writeLock().lock();
    //TODO 写日记
    index.put(row.getEntries().get(primaryIndex), row);
    this.lock.writeLock().unlock();
  }

  public void delete(Row row) {
    // TODO
    Entry entry = row.getEntries().get(primaryIndex);
    this.lock.writeLock().lock();
    index.remove(entry);
    this.lock.writeLock().unlock();
    //TODO 写日记
  }

  public void update(Row row) {
    // TODO
    Entry entry = row.getEntries().get(primaryIndex);
    this.lock.writeLock().lock();
    index.update(entry, row);
    this.lock.writeLock().unlock();
  }

  private void serialize(String filename) throws IOException {
    // TODO 改进暂时输出当前table所有row的不足
    FileOutputStream fileOut = new FileOutputStream(filename);
    ObjectOutputStream ooStream = new ObjectOutputStream(fileOut);
    ArrayList<Row> rows = new ArrayList<>();
    BPlusTreeIterator<Entry, Row> iterator = index.iterator();
    while (iterator.hasNext()) {
      ooStream.writeObject(iterator.next());
    }
    ooStream.close();
  }

  private ArrayList<Row> deserialize(String filename) throws IOException, ClassNotFoundException, ClassCastException{
    // TODO
    FileInputStream fileIn = new FileInputStream(filename);
    ObjectInputStream oiStream = new ObjectInputStream(fileIn);
    Object obj = oiStream.readObject();
    ArrayList<Row> rows = null;
    rows = (ArrayList<Row>) Global.castList(obj, Row.class);
    oiStream.close();
    return rows;
  }

  private class TableIterator implements Iterator<Row> {
    private Iterator<Pair<Entry, Row>> iterator;

    TableIterator(Table table) {
      this.iterator = table.index.iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Row next() {
      return iterator.next().getValue();
    }
  }

  @Override
  public Iterator<Row> iterator() {
    return new TableIterator(this);
  }
}
