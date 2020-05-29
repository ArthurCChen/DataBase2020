package cn.edu.thssdb.type;

import com.sun.org.apache.xpath.internal.operations.Number;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;

public enum ColumnType implements Serializable {
  INT(){
    @Override
    public int getBytes() {
      return Integer.BYTES;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new IntValue(dis.readInt());
      } catch (IOException e){
        throw new Exception();
      }
    }
  }, LONG(){
    @Override
    public int getBytes() {
      return Long.BYTES;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new LongValue(dis.readLong());
      } catch (IOException e){
        throw new Exception();
      }
    }
  }, FLOAT(){
    @Override
    public int getBytes() {
      return Float.BYTES;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new FloatValue(dis.readFloat());
      } catch (IOException e){
        throw new Exception();
      }
    }
  }, DOUBLE(){
    @Override
    public int getBytes() {
      return Double.BYTES;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        return new DoubleValue(dis.readDouble());
      } catch (IOException e){
        throw new Exception();
      }
    }
  }, STRING(){
    @Override
    public int getBytes() {
      return Integer.BYTES;
    }

    @Override
    public ColumnValue parse(DataInputStream dis, int maxLen) throws Exception {
      try{
        int len = dis.readInt();
        byte s[] = new byte[len];
        dis.read(s);
        dis.skipBytes(maxLen - len);
        return new StringValue(new String(s), maxLen);
      } catch (IOException e){
        throw new Exception();
      }
    }
  };

  public abstract  int getBytes();

  public abstract  ColumnValue parse (DataInputStream dis, int maxLen) throws  Exception;

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
      throw new Exception();
    }
  }
};

