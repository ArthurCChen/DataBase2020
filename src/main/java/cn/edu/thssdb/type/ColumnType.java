package cn.edu.thssdb.type;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;

public enum ColumnType implements Serializable {
  INT(){
    @Override
    public int getBytes() {
      return Integer.BYTES + 1;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new IntValue(dis.readInt(), dis.readBoolean());
      } catch (IOException e){
        throw e;
      }
    }

    @Override
    public Object getDefault() {
      return (int)0;
    }
  }, LONG(){
    @Override
    public int getBytes() {
      return Long.BYTES + 1;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new LongValue(dis.readLong(), dis.readBoolean());
      } catch (IOException e){
        throw e;
      }
    }

    @Override
    public Object getDefault() {
      return (long)0;
    }
  }, FLOAT(){
    @Override
    public int getBytes() {
      return Float.BYTES + 1;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new FloatValue(dis.readFloat(), dis.readBoolean());
      } catch (IOException e){
        throw e;
      }
    }

    @Override
    public Object getDefault() {
      return (float)0;
    }
  }, DOUBLE(){
    @Override
    public int getBytes() {
      return Double.BYTES + 1;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new DoubleValue(dis.readDouble(), dis.readBoolean());
      } catch (IOException e){
        throw e;
      }
    }

    @Override
    public Object getDefault() {
      return (double)0;
    }
  }, STRING(){
    @Override
    public int getBytes() {
      return Integer.BYTES + 1;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        int len = dis.readInt();
        byte s[] = new byte[len];
        dis.read(s);
        dis.skipBytes(maxLen - len);
        boolean isNotNull = dis.readBoolean();
        return new StringValue(new String(s), maxLen, isNotNull);
      } catch (IOException e){
        throw e;
      }
    }

    @Override
    public Object getDefault() {
      return new String("");
    }
  };

  public abstract  int getBytes();

  public abstract  ColumnValue parse (DataInputStream dis, int maxLen) throws  Exception;

  public abstract Object getDefault();

  public static  ColumnType getType(String type) throws  Exception{
    if(type.toUpperCase().equals("INT")){
      return INT;
    }else if(type.toUpperCase().equals("LONG")){
      return LONG;
    }else if(type.toUpperCase().equals("FLOAT")){
      return FLOAT;
    }else if(type.toUpperCase().equals("DOUBLE")){
      return DOUBLE;
    }else if(type.toUpperCase().equals("STRING")){
      return STRING;
    }else{
      throw new Exception("this type cannot defined");
    }
  }
};

