package cn.edu.thssdb.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class Row implements Serializable {
  private static final long serialVersionUID = -5809782578272943999L;
  protected ArrayList<Entry> entries;

  public Row() {
    this.entries = new ArrayList<>();
  }

  public Row(Entry[] entries) {
    this.entries = new ArrayList<>(Arrays.asList(entries));
  }

  public Row(ArrayList<Entry> entries) {
    this.entries = entries;
  }

  public ArrayList<Entry> getEntries() {
    return entries;
  }

  public int size() {
    return entries.size();
  }

  public void appendEntries(ArrayList<Entry> entries) {
    this.entries.addAll(entries);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Row) && toString().equals(obj.toString());
  }

  public String toString() {
    if (entries == null)
      return "EMPTY";
    StringJoiner sj = new StringJoiner(", ");
    for (Entry e : entries)
      sj.add(e.toString());
    return sj.toString();
  }
}
