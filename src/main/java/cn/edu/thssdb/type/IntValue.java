package cn.edu.thssdb.type;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.DataOutputStream;
import java.io.IOException;

public class IntValue extends NumberValue {
    public IntValue(int i){
        super(i, ColumnType.INT);
    }

    public IntValue(int i, boolean isNotNull){
        super(i, ColumnType.INT, isNotNull);
    }


    @Override
    public boolean greater_than(ColumnValue val) {
        return getValue() > ((IntValue)val).getValue();
    }

    @Override
    public boolean less_than(ColumnValue val) {
        return getValue() < ((IntValue)val).getValue();
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeInt(value.intValue());
        dos.writeBoolean(isNotNull);
    }

    @Override
    public boolean equals(Object obj) {
        return getValue() == ((IntValue)obj).getValue();
    }

    public Integer getValue(){
        return value.intValue();
    }

}
