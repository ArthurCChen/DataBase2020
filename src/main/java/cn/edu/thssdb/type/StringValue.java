package cn.edu.thssdb.type;

import cn.edu.thssdb.utils.Global;

import java.io.DataOutputStream;
import java.io.IOException;

public class StringValue implements ColumnValue{
    private static final long serialVersionUID = 1L;

    private final String value;
    private final int maxLen;
    private final boolean isNotNull;

    public StringValue(String s, int maxLen, boolean isNotNull){
        this.maxLen = maxLen;
        this.isNotNull = isNotNull;

        if(s.length() > maxLen)//TODO throw Exception?
            value = s.substring(0, maxLen);
        else
            value = s;
    }

    public StringValue(String s, int maxLen){
        this(s, maxLen, true);
    }

    public String getValue(){
        return value;
    }

    @Override
    public String toString()
    {
        if(! isNotNull)
            return Global.NULL_VALUE_DISPLAY;
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
        dos.writeBoolean(isNotNull);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int compareTo(ColumnValue o) {
        return value.compareTo(((StringValue)o).value);
    }

    @Override
    public int getMaxLen() {
        return maxLen;
    }
}
