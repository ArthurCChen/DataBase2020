package cn.edu.thssdb.schema;

import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.type.ColumnValue;
import cn.edu.thssdb.type.ValueFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

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
      ColumnValue val = ValueFactory.getField(0, column.getType(), column.getMaxLength());
      entries.add(new Entry(val));
      }
  }

  public ColumnValue getColumnValue(int i){
    return this.getEntries().get(i).value;
  }

  public RowDesc getRowDesc() {
    return rowDesc;
  }

  public Row(RowDesc desc, ArrayList<String> attrNames, ArrayList<Object> values) throws  Exception{
    this(desc);
    if (attrNames == null) {
      //attrNames are all the attribute names of the table
      attrNames = desc.getAttrNames();
    }
    if (attrNames.size() != values.size()) {
      throw new Exception();
    } else {
      HashMap<String, Object> hashMap = new HashMap<>();
      for (int i=0; i<attrNames.size(); i++) {
        hashMap.put(attrNames.get(i), values.get(i));
      }
      for (int i = 0; i < desc.getColumnSize(); i ++){
        Column item = desc.get(i);
        String attr = desc.get(i).getName();
        if(hashMap.containsKey(attr)){
          ColumnValue val = ValueFactory.getField(values.get(i));
          setValue(i, val);
        }else if(item.getPrimary() == Column.PRIMARY || item.isNotNull()){
          throw new Exception();
        }
      }
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

  public void serialize(DataOutputStream dos) throws IOException {
    for (Entry entry: entries) {
      entry.value.serialize(dos);
    }
  }

  public void setValue(int i, ColumnValue val){
    this.entries.set(i, new Entry(val));
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
