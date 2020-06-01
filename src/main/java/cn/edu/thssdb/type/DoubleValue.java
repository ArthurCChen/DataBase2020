package cn.edu.thssdb.type;

import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleValue extends NumberValue{

    public DoubleValue(Double i){
        super(i, ColumnType.DOUBLE);
    }

    public DoubleValue(Double i, boolean isNotNull){
        super(i, ColumnType.DOUBLE, isNotNull);

    }

    @Override
    public boolean greater_than(ColumnValue val) {
        return value.doubleValue() > ((DoubleValue)val).getValue();
    }

    @Override
    public boolean less_than(ColumnValue val) {
        return value.doubleValue() < ((DoubleValue)val).getValue();
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeDouble(value.doubleValue());
        dos.writeBoolean(isNotNull);
    }

    @Override
    public boolean equals(Object obj) {
        return this.value == ((DoubleValue)obj).getValue();
    }

    public Double getValue(){
        return value.doubleValue();
    }
}
