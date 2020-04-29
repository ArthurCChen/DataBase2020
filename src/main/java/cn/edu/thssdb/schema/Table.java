package cn.edu.thssdb.schema;

import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.index.BPlusTreeIterator;
import cn.edu.thssdb.utils.Global;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Iterable<Row> {
  ReentrantReadWriteLock lock;
  private String databaseName;
  public String tableName;
  public ArrayList<Column> columns;
  public BPlusTree<Entry, Row> index;
  private int primaryIndex;

  public Table(String databaseName, String tableName, Column[] columns) {
    // TODO
  }

  private void recover() {
    // TODO
  }

  public void insert() {
    // TODO
  }

  public void delete() {
    // TODO
  }

  public void update() {
    // TODO
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
