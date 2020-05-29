package cn.edu.thssdb.storage;

import cn.edu.thssdb.schema.Row;

public interface FileIterator {

    public boolean hasNext();

    public Row next();

    public void open();

    public void rewind();

    public void close();
}
