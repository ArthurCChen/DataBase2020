package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.storage.PageId;

public class HeapPageId implements PageId {

    private final int tid;
    private final int pageNumber;

    public HeapPageId(int tid, int pageNumber){
        this.tid = tid;
        this.pageNumber = pageNumber;
    }

    @Override
    public int[] serialize() {
        int data[] = new int[2];

        data[0] = getTableId();
        data[1] = getPageNumber();

        return data;
    }

    @Override
    public int getTableId() {
        return tid;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int hashCode() {
        String s = String.format("%032d %032d", this.pageNumber, this.tid);
        return s.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof HeapPageId)){
            return false;
        }else{
            HeapPageId btpid = (HeapPageId) obj;
            return tid == btpid.tid &&
                    pageNumber == btpid.pageNumber;
        }
    }
}
