package cn.edu.thssdb.schema;

import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.type.ColumnValue;
import cn.edu.thssdb.type.ValueFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

public class Row implements Serializable {
  private static final long serialVersionUID = -5809782578272943999L;
  protected ArrayList<Entry> entries;
  private RowDesc rowDesc;
  private PageId pageId;
  private int rowId;

  public Row(Entry[] entries) {
    this.entries = new ArrayList<>(Arrays.asList(entries));
  }

  public Row(RowDesc desc){
  this.rowDesc = desc;
  entries = new ArrayList<>();
  for(int i = 0; i < desc.getColumnSize(); i ++){
      Column column = desc.get(i);
      ColumnValue val = ValueFactory.getField(0, column.getType(), column.getMaxLength())
      entries.add(new Entry(val));
      }
  }

  public ArrayList<Entry> getEntries() {
    return entries;
  }

  public void setRowId(int rowId) {
    this.rowId = rowId;
  }

  public int getRowId() {
    return rowId;
  }

  public void setPageId(PageId pageId) {
    this.pageId = pageId;
  }

  public PageId getPageId() {
    return pageId;
  }

  public void appendEntries(ArrayList<Entry> entries) {
    this.entries.addAll(entries);
  }

  public String toString() {
    if (entries == null)
      return "EMPTY";
    StringJoiner sj = new StringJoiner(", ");
    for (Entry e : entries)
      sj.add(e.toString());
    return sj.toString();
  }

  public Iterator<Entry> iterator(){
    return this.entries.iterator();
  }
}
