package cn.edu.thssdb.storage.Heap;


import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.storage.*;
import cn.edu.thssdb.storage.Heap.HeapFileIterator;
import cn.edu.thssdb.storage.Heap.HeapPage;
import cn.edu.thssdb.storage.Heap.HeapPageId;
import cn.edu.thssdb.type.ColumnValue;
import cn.edu.thssdb.utils.Global;
import sun.security.action.GetLongAction;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

public class HeapFile implements FileHandler {

    private File file;
    private int id;
    private RowDesc tupleDesc;
    private boolean hasPrimaryKeyConstraint=true;


    public HeapFile(int id, File file, RowDesc tupleDesc, boolean hasPrimaryKeyConstraint){
        this.id = id;
        this.file = file;
        this.tupleDesc = tupleDesc;
        this.hasPrimaryKeyConstraint = hasPrimaryKeyConstraint;
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
                    throw new Exception();
                }
            }
        }
    }


    public ArrayList<Page> insertRow(Row t){
        ArrayList<Page> affectedPages = new ArrayList<>();
        try {
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
        } catch (Exception e){
            e.printStackTrace();
        }
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


    public ArrayList<Page> deleteRow(Row t) {
        ArrayList<Page> affectedPages = new ArrayList<>();
        try {
            PageId pageId = t.getPageId();
            HeapPage page = (HeapPage) Global.gBufferPool().getPage(pageId);
            page.deleteRow(t);
            affectedPages.add(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return affectedPages;
    }

    @Override
    public ArrayList<Page> updateRow(Row row) {
        return null;
    }

    public FileIterator iterator() {
        return new HeapFileIterator(this);
    }

    @Override
    public RowDesc getMeta() {
        return null;
    }
}


