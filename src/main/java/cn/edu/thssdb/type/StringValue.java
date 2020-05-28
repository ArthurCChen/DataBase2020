package cn.edu.thssdb.type;

import java.io.DataOutputStream;
import java.io.IOException;

public class StringValue implements ColumnValue{
    private static final long serialVersionUID = 1L;

    private final String value;
    private final int maxLen;

    public StringValue(String s, int maxLen){
        this.maxLen = maxLen;

        if(s.length() > maxLen)//TODO throw Exception?
            value = s.substring(0, maxLen);
        else
            value = s;
    }

    public String getValue(){
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getValue().equals(((StringValue)obj).getValue());
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        String s = value;
        if(s.length() > maxLen)
            throw new IOException();
        int overflow = maxLen - s.length();
        dos.writeInt(s.length());
        dos.writeBytes(s);
        while(overflow -- > 0)
            dos.write((byte)0);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }
}
