package cn.edu.thssdb.storage;

import cn.edu.thssdb.schema.TableMeta;

import java.io.*;

public class BPTRootPage implements Page{
    protected boolean dirty = false;

    protected final static int PAGE_SIZE = 9;

    protected final BPTPageId id;

    private int root;
    private int rootCategory;
    private int header;

    protected byte[] oldDate;

    public BPTRootPage(BPTPageId id, byte[]data) throws IOException {
        this.id = id;

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

        root = dis.readInt();
        rootCategory = (int) dis.readByte();

        header = dis.readInt();
        oldDate = getData().clone();
    }

    @Override
    public BPTPageId getId() {
        return id;
    }

    public static BPTPageId getId(int tid){
        return new BPTPageId(tid, 0, BPTPageId.ROOT_PTR);
    }

    @Override
    public void markDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(PAGE_SIZE);
        DataOutputStream dos = new DataOutputStream(baos);
        try{
            dos.writeInt(root);
            dos.writeByte((byte) rootCategory);
            dos.writeInt(header);
            dos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    public BPTPageId getRootId(){
        if(root == 0)
            return null;
        return new BPTPageId(id.getTableId(), root, rootCategory);
    }

    public void setRootId(BPTPageId id){
        if(id == null) root = 0;
        else{
            root = id.getPageNumber();
            rootCategory = id.getPageCatag();
        }
    }

    public BPTPageId getHeaderId(){
        if(header == 0)
            return null;
        return new BPTPageId(id.getTableId(), header, BPTPageId.HEADER);
    }

    public void setHeaderId(BPTPageId id){
        if(id == null) header = 0;
        else{
            header = id.getPageNumber();
        }
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }
}
