package cn.edu.thssdb.type;

import java.io.DataOutputStream;
import java.io.IOException;

public class FloatValue extends NumberValue{
    public FloatValue(float i){
        super(i, ColumnType.FLOAT);
    }

    @Override
    public boolean greater_than(ColumnValue val) {
        return getValue() > ((FloatValue)val).getValue();
    }

    @Override
    public boolean less_than(ColumnValue val) {
        return getValue() < ((FloatValue)val).getValue();
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeDouble(value.floatValue());
    }

    @Override
    public boolean equals(Object obj) {
        return getValue() == ((FloatValue)obj).getValue();
    }

    public Float getValue(){
        return value.floatValue();
    }

}
