package cn.edu.thssdb.storage.BPT;

import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.Page;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;
//
//public class BPTFileHandler extends BPlusTree implements FileHandler {
//
//    private final File file;
//    private final String id;
//
//    BPTFileHandler(String id, File f){
//        this.file = f;
//        this.id = id;
//    }
//
//
//
//    @Override
//    public Page readPage(String id) {
//        return null;
//    }
//
//    @Override
//    public void writePage(Page p) {
//
//    }
//
//    @Override
//    public ArrayList<Page> insertRow(Row row) {
//        return null;
//    }
//
//    @Override
//    public ArrayList<Page> deleteRow(Row row) {
//        return null;
//    }
//
//    @Override
//    public ArrayList<Page> updateRow(Row row) {return null;}
//
//    @Override
//    public String getId() {
//        return this.id;
//    }
//
//    @Override
//    public RowDesc getMeta() {
//        return null;
//    }
//
//    @Override
//    public void forEach(Consumer action) {
//
//    }
//
////    @Override
////    public Spliterator spliterator() {
////        return null;
////    }
//}
