package cn.edu.thssdb.storage;

import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Row;
import oracle.jrockit.jfr.StringConstantPool;

import java.io.File;
import java.util.ArrayList;

public class BPTFileHandler extends BPlusTree implements FileHandler{

    private final File file;
    private final String id;

    BPTFileHandler(String id, File f){
        this.file = f;
        this.id = id;
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
    public ArrayList<Page> updateRow(Row row) {return null;}

    @Override
    public String getId() {
        return this.id;
    }
}
