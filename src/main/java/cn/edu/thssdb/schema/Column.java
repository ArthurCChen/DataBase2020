package cn.edu.thssdb.schema;

import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ColumnValue;

import java.io.DataInputStream;
import java.io.Serializable;

public class Column implements Comparable<Column>, Serializable {

  private static final long serialVersionUID = 1L;

  private String name;
  private ColumnType type;
  private boolean primary;
  private boolean notNull;
  private int maxLength;
  public String tableName;

  static public boolean PRIMARY;
  static public boolean NOT_PRIMARY;

  static {
    NOT_PRIMARY = false;
    PRIMARY = true;
  }

  public Column(String name, ColumnType type, boolean primacy ,boolean notNull, int maxLength){
    this(name, type, notNull, maxLength);
    this.primary = primacy;
  }

  public Column(String name, ColumnType type, boolean notNull, int maxLength) {
    this.name = name;
    this.type = type;
    this.notNull = notNull;
    if (type == ColumnType.STRING) {
      this.maxLength = maxLength;
    }else{
      this.maxLength = 0;
    }
    this.primary = NOT_PRIMARY;
  }

  public Column(String name, ColumnType type, boolean notNull) {
    this(name, type, notNull, 0);
  }

  public Column(String name, ColumnType type, boolean notNull, int maxLength, String tableName){
    this(name, type, notNull, maxLength);
    this.tableName = tableName;
  }

  public boolean isNotNull() {
    return notNull;
  }

  public void setPrimary(boolean primary) {
    this.primary = PRIMARY;
  }

  // name is unnecessary
  public boolean equals(Object obj){
    if(!(obj instanceof  Column))
      return false;
    Column other = (Column)obj;
    return (primary == other.primary) && (type == other.type)  && (notNull == other.notNull) && (maxLength == other.maxLength);
  }

  @Override
  public int compareTo(Column e) {
    return name.compareTo(e.name);
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    if(type == ColumnType.STRING) {
      stringBuilder.append(String.format("%s(%s(%d)", name, type, maxLength));

    }else{
      stringBuilder.append(String.format("%s(%s", name, type));
    }
    if(notNull){
      stringBuilder.append(" not null");
    }
    stringBuilder.append(")");
    return stringBuilder.toString();
  }

  public boolean getPrimary() {
    return primary;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNotNull(boolean notnull) {
    this.notNull = notnull;
  }

  public String getFullName(){
    if(tableName != null){
      return tableName + "." + name;
    }else{
      return name;
    }
  }

  public ColumnType getType() {
    return type;
  }

  public int getByteSize(){
    return type.getBytes() + this.maxLength;
  }

  public ColumnValue parse(DataInputStream dis) throws Exception{
    return this.type.parse(dis, maxLength);
  }

  public boolean isTableName(String tableName){
    return this.tableName != null && this.tableName.equals(tableName);
  }

  public boolean isName(String name){
    return this.name.equals(name);
  }

  public boolean isName(String tableName, String columnName){
    return isName(columnName) && isTableName(tableName);
  }

  public String getTable_name() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }



  public int getMaxLength() {
    return maxLength;
  }
}
