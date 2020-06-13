package cn.edu.thssdb.schema;

import cn.edu.thssdb.type.ColumnValue;

import java.io.Serializable;

public class Entry implements Serializable {
  private static final long serialVersionUID = -5809782578272943999L;
  public final ColumnValue value;

  public Entry(ColumnValue value) {
    this.value = value;
  }


  public int compareTo(Entry e) {
    return value.getValue().compareTo(e.value.getValue());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (this.getClass() != obj.getClass())
      return false;
    Entry e = (Entry) obj;
    boolean result = value.equals(e.value);
    return result;
  }



  public String toString() {
    return value.toString();
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
