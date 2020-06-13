package cn.edu.thssdb.storage.Heap;


import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.exception.DuplicateKeyException;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.type.ColumnValue;
import cn.edu.thssdb.utils.Global;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class HeapFile implements FileHandler {

    private File file;
    private File indexFile;
    private int tid;
    private RowDesc tupleDesc;
    private boolean hasPrimaryKeyConstraint=true;
    private BPlusTree<ColumnValue, HeapIndexEntry> primaryIndex;

    public HeapFile(int id, File file, File indexFile, RowDesc tupleDesc) throws IOException{
        this.tid = id;
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

    public void persistIndex() throws IOException{

        DataOutputStream dos = new DataOutputStream(new FileOutputStream(indexFile));
        for(Pair<ColumnValue, HeapIndexEntry> entry: primaryIndex){
            entry.getKey().serialize(dos);
            dos.writeInt(entry.getValue().pageNumber);
            dos.writeShort(entry.getValue().offset);
        }
    }

    private Row getRow(int pageNum, short offset) {
        PageId pid = new HeapPageId(getTid(), pageNum);
        HeapPage page = (HeapPage) Global.gBufferPool().getPage(pid);
        Row row = page.getRowByOffset(offset);
        return row;
    }

    public File getFile() {
        return this.file;
    }


    public int getTid() {
        return this.tid;
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
        ColumnValue primary = t.getPrimaryValue();
        if(primaryIndex.contains(primary)) {
            throw new DuplicateKeyException();
        }
//        int tableId = this.getTid();
//        int numPages = this.numPages();
//        int primaryKeyIdx = t.getRowDesc().getPrimaryIndex().get(0);
//        ColumnValue pkField = t.getEntries().get(primaryKeyIdx).value;
//        for (int i = 0; i < numPages; i++) {
//            HeapPageId pageId = new HeapPageId(tableId, i);
//            Page page = Global.gBufferPool().getPage(pageId);
//            Iterator<Row> iterator = ((HeapPage)page).iterator();
//            while (iterator.hasNext()) {
//                Row tuple = iterator.next();
//                if (tuple.getColumnValue(primaryKeyIdx).equals(pkField)){
//                    throw new Exception("primary key clash");
//                }
//            }
//        }
    }


    public ArrayList<Page> insertRow(Row t)throws  Exception{
        ArrayList<Page> affectedPages = new ArrayList<>();

        if (hasPrimaryKeyConstraint) {
            this.checkPrimaryKeyViolated(t); //Check whether the Row satisfies the Primary Key
        }
        HeapPage heapPage = (HeapPage)this.getEmptyPage(0);
        if (heapPage == null) {
            HeapPageId heapPageId = new HeapPageId(this.getTid(), this.numPages());
            heapPage = new HeapPage(heapPageId, HeapPage.createEmptyPageData(), tupleDesc);
            this.writePage(heapPage);
            heapPage = (HeapPage) Global.gBufferPool().getPage(heapPageId);
        }
        heapPage.insertRow(t);
        affectedPages.add(heapPage);

        HeapIndexEntry heapIndexEntry = new HeapIndexEntry(t.getPrimaryValue(), t.getPageId().getPageNumber(), t.getPageOffset());
        primaryIndex.put(t.getPrimaryValue(), heapIndexEntry);
        //利用日志保存当前添加了哪些行

        return affectedPages;
    }


    public Page getEmptyPage(int start)
            throws Exception {
        try {
            int tableId = this.getTid();
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

    private void checkRowOffset(Row t){
        if(t.getPageOffset() < 0){
            HeapIndexEntry entry = primaryIndex.get(t.getPrimaryValue());
            t.setPageOffset(entry.offset);
            t.setPageId(new HeapPageId(tid, entry.pageNumber));
        }
    }

    public ArrayList<Page> deleteRow(Row t) throws  Exception {

        ArrayList<Page> affectedPages = new ArrayList<>();
        PageId pageId = t.getPageId();
        HeapPage page = (HeapPage) Global.gBufferPool().getPage(pageId);
        page.deleteRow(t);
        affectedPages.add(page);

        //利用日志保存当前删除了哪些行
        primaryIndex.remove(t.getPrimaryValue());

        return affectedPages;
    }

    @Override
    public ArrayList<Page> updateRow(Row t) throws  Exception {
        ArrayList<Page> affectedPages = new ArrayList<>();
        PageId pageId = t.getPageId();
        HeapPage page = (HeapPage) Global.gBufferPool().getPage(pageId);
        page.updateRow(t);
        affectedPages.add(page);

        //日志

        return affectedPages;
    }

    public FileIterator iterator() {
        return new HeapFileIterator(this);
    }

    @Override
    public RowDesc getMeta() {
        return null;
    }
}


