package cn.edu.thssdb.type;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public interface ColumnValue extends Serializable, Comparable<ColumnValue> {
    void serialize(DataOutputStream dos) throws IOException;

    public ColumnType getType();

    public Comparable getValue();

    public int getMaxLen();

}
