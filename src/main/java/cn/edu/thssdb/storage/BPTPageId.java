package cn.edu.thssdb.storage;

public class BPTPageId implements PageId {
    public final static int ROOT_PTR = 0;
    public final static int INTERNAL = 1;
    public final static int LEAF = 2;
    public final static int HEADER = 3;

    private final int tid;
    private final int pageNumber;
    private int pageCatag;

    public BPTPageId(int tid, int pageNumber, int pageCatag) {
        this.tid = tid;
        this.pageNumber = pageNumber;
        this.pageCatag = pageCatag;
    }

    @Override
    public int getTableId() {
        return this.tid;
    }

    @Override
    public int getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public int hashCode(){
        return (this.tid << 16) + (pageNumber << 2) + pageCatag;
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof BPTPageId))
            return false;
        BPTPageId btpid = (BPTPageId) obj;
        return tid == btpid.tid &&
                pageNumber == btpid.pageNumber &&
                pageCatag == btpid.pageCatag;
    }

    @Override
    public int[] serialize(){
        int data[] = new int[3];
        data[0] = tid;
        data[1] = pageNumber;
        data[2] = pageCatag;
        return data;
    }

    public int getPageCatag(){
        return this.pageCatag;
    }


}
