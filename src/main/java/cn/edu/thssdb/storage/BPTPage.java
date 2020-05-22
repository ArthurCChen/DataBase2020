package cn.edu.thssdb.storage;

import cn.edu.thssdb.schema.TableMeta;
import cn.edu.thssdb.utils.Global;

public class BPTPage implements Page {
    protected boolean dirty = false;

    protected final static int INDEX_SIZE = 4;

    protected final BPTPageId id;
    protected final int keyField;
    protected final TableMeta tm;

    protected int parent;
    protected byte[] oldDate;
    protected final Byte oldDataLock = new Byte((byte)0);


    BPTPage(BPTPageId id, int key){
        this.id = id;
        this.keyField = key;
        this.tm = Global.getTableFromTid(id.getTableId()).getTableMeta();
    }


    @Override
    public PageId getId() {
        return id;
    }

    @Override
    public void markDirty(boolean dirty) {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
