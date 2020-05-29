package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Page;

import java.io.File;
import java.util.ArrayList;

public class HeapFile implements FileHandler {

    private File file;
    private int id;
    private RowDesc meta;

    public HeapFile(int id, File file, RowDesc meta){
        this.id = id;
        this.file = file;
        this.meta = meta;
    }

    @Override
    public Page readPage(String id) {
        return null;
    }

    @Override
    public void writePage(Page p) {

    }

    @Override
    public ArrayList<Page> insertRow(Row row) {
        return null;
    }

    @Override
    public ArrayList<Page> deleteRow(Row row) {
        return null;
    }

    @Override
    public ArrayList<Page> updateRow(Row row) {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public FileIterator iterator() {
        return null;
    }

    @Override
    public RowDesc getMeta() {
        return null;
    }
}
