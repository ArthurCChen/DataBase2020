package cn.edu.thssdb.storage.Heap;


import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.type.ColumnValue;
import cn.edu.thssdb.utils.Global;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class HeapFile implements FileHandler {

    private File file;
    private File indexFile;
    private int id;
    private RowDesc tupleDesc;
    private boolean hasPrimaryKeyConstraint=true;
    private BPlusTree<ColumnValue, HeapIndexEntry> primaryIndex;

    public HeapFile(int id, File file, File indexFile, RowDesc tupleDesc) throws IOException{
        this.id = id;
        this.indexFile = indexFile;
        this.file = file;
        this.tupleDesc = tupleDesc;
        recoverIndex();
    }

    public void recoverIndex() throws IOException{

        primaryIndex = new BPlusTree<ColumnValue, HeapIndexEntry>();
        DataInputStream dis = new DataInputStream(new FileInputStream(indexFile));
        while(dis.available() != 0){
            HeapIndexEntry entry = HeapIndexEntry.parse(dis, tupleDesc.getPrimaryType(), tupleDesc.getPrimaryMaxLen());
            primaryIndex.put(entry.primary, entry);
        }
    }


    public File getFile() {
        return this.file;
    }


    public int getId() {
        return this.id;
    }


    public RowDesc getRowDesc() {
        return this.tupleDesc;
    }


    public Page readPage(PageId pid) {
        try{
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "r");
            int offset = pid.getPageNumber() * Global.pageSize;
            byte[] bytes = new byte[Global.pageSize];
            randomAccessFile.seek(offset);
            randomAccessFile.read(bytes, 0, Global.pageSize);
            HeapPageId heapPageId = (HeapPageId) pid;
            randomAccessFile.close();
            return new HeapPage(heapPageId, bytes, tupleDesc);
        }catch (IOException exception){
            exception.printStackTrace();
        }
        return null;
    }


    public void writePage(Page page){
        try {
            HeapPageId pageId = (HeapPageId)page.getId();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            int offset = pageId.getPageNumber()* Global.pageSize;
            byte[] bytes = page.getData();
            randomAccessFile.seek(offset);
            randomAccessFile.write(bytes, 0, Global.pageSize);
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int numPages() {
        return (int)Math.ceil((float)file.length() / Global.pageSize);
    }


    public void checkPrimaryKeyViolated(Row t) throws Exception{
        int tableId = this.getId();
        int numPages = this.numPages();
        int primaryKeyIdx = t.getRowDesc().getPrimaryIndex().get(0);
        ColumnValue pkField = t.getEntries().get(primaryKeyIdx).value;
        for (int i = 0; i < numPages; i++) {
            HeapPageId pageId = new HeapPageId(tableId, i);
            Page page = Global.gBufferPool().getPage(pageId);
            Iterator<Row> iterator = ((HeapPage)page).iterator();
            while (iterator.hasNext()) {
                Row tuple = iterator.next();
                if (tuple.getColumnValue(primaryKeyIdx).equals(pkField)){
                    throw new Exception("primary key clash");
                }
            }
        }
    }


    public ArrayList<Page> insertRow(Row t)throws  Exception{
        ArrayList<Page> affectedPages = new ArrayList<>();

        if (hasPrimaryKeyConstraint) {
            this.checkPrimaryKeyViolated(t); //Check whether the Row satisfies the Primary Key
        }
        HeapPage heapPage = (HeapPage)this.getEmptyPage(0);
        if (heapPage == null) {
            HeapPageId heapPageId = new HeapPageId(this.getId(), this.numPages());
            heapPage = new HeapPage(heapPageId, HeapPage.createEmptyPageData(), tupleDesc);
            this.writePage(heapPage);
            heapPage = (HeapPage) Global.gBufferPool().getPage(heapPageId);
        }
        heapPage.insertRow(t);
        affectedPages.add(heapPage);

        return affectedPages;
    }


    public Page getEmptyPage(int start)
            throws Exception {
        try {
            int tableId = this.getId();
            int numPages = this.numPages();
            for (int i = start; i < numPages; i++) {
                HeapPageId pageId = new HeapPageId(tableId, i);
                Page page = Global.gBufferPool().getPage(pageId);
                if (((HeapPage)page).getNumEmptySlots() != 0){
                    return page;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public ArrayList<Page> deleteRow(Row t) throws  Exception {
        ArrayList<Page> affectedPages = new ArrayList<>();
        PageId pageId = t.getPageId();
        HeapPage page = (HeapPage) Global.gBufferPool().getPage(pageId);
        page.deleteRow(t);
        affectedPages.add(page);
        return affectedPages;
    }

    @Override
    public ArrayList<Page> updateRow(Row t) throws  Exception{
        throw new InternalException("updateRow ot implemented");
        //        ArrayList<Page> affectedPages = new ArrayList<>();
//        try {
//            PageId pageId = t.getPageId();
//            HeapPage page = (HeapPage) Global.gBufferPool().getPage(pageId);
//            page.deleteRow(t);
//            affectedPages.add(page);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return affectedPages;
    }

    public FileIterator iterator() {
        return new HeapFileIterator(this);
    }

    @Override
    public RowDesc getMeta() {
        return null;
    }
}


