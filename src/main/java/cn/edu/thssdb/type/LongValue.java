package cn.edu.thssdb.type;

import java.io.DataOutputStream;
import java.io.IOException;

public class LongValue extends  NumberValue{
    public LongValue(long i){
        super(i, ColumnType.LONG);
    }

    public LongValue(long i, boolean isNotNull){
        super(i, ColumnType.LONG, isNotNull);
    }

    @Override
    public boolean greater_than(ColumnValue val) {
        return getValue() > ((LongValue)val).getValue();
    }

    @Override
    public boolean less_than(ColumnValue val) {
        return getValue() < ((LongValue)val).getValue();
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeLong(value.longValue());
        dos.writeBoolean(isNotNull);
    }

    @Override
    public boolean equals(Object obj) {
        return getValue() == ((LongValue)obj).getValue();
    }

    public Long getValue(){
        return value.longValue();
    }
}
