package cn.edu.thssdb.adapter;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.storage.FileIterator;

import java.util.Iterator;

public class HeapTableIterator implements Iterator<Row> {

    FileIterator iter;
    public HeapTableIterator(FileIterator iter){
        this.iter = iter;
    }


    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Row next() {
        return iter.next();
    }
}
