package cn.edu.thssdb.type;

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
        // to: LI SIYU
        // the line is previously:
        //     return getValue() == ((IntValue)obj).getValue();
        // which is a bug, since == do not work for all integers, please take care when merge.
        return getValue().equals(((IntValue)obj).getValue());
    }

    public Integer getValue(){
        return value.intValue();
    }

}
