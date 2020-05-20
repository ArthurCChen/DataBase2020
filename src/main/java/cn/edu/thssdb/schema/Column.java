package cn.edu.thssdb.schema;

import cn.edu.thssdb.type.ColumnType;

public class Column implements Comparable<Column> {
  private String name;
  private ColumnType type;
  private boolean primary;

  private boolean notNull;

  private int maxLength;
  private String table_name = null;
  public Column(String name, ColumnType type, boolean primary, boolean notNull, int maxLength) {
    this.name = name;
    this.type = type;
    this.primary = primary;
    this.notNull = notNull;
    this.maxLength = maxLength;
  }

  public boolean isNotNull() {
    return notNull;
  }

  public void setNotNull(boolean notNull) {
    this.notNull = notNull;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public boolean isPrimary() {
    return this.primary;
  }

  public ColumnType getType() { return this.type; }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(ColumnType type) {
    this.type = type;
  }

  public String getTable_name() {
    return table_name;
  }

  public void setTable_name(String table_name) {
    this.table_name = table_name;
  }

  @Override
  public int compareTo(Column e) {
    return name.compareTo(e.name);
  }

  public String toString() {
    return name + ',' + type + ',' + primary + ',' + notNull + ',' + maxLength;
  }
}
