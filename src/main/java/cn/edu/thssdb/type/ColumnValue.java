package cn.edu.thssdb.type;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public interface ColumnValue extends Serializable {
    void serialize(DataOutputStream dos) throws IOException;

    public ColumnType getType();

    Comparable getValue();
}
