package cn.edu.thssdb.storage.BPT;

import cn.edu.thssdb.schema.TableMeta;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.utils.Global;

public abstract class BPTPage implements Page {
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

    public static byte[] createEmptyPageData(){
        return new byte[Global.pageSize];
    }

    public BPTPageId getParentId(){
        if(parent == 0){
            return BPTRootPage.getId(id.getTableId());
        }
        return new BPTPageId(id.getTableId(), parent, BPTPageId.INTERNAL);
    }

    public void setParent(BPTPageId id) {
        if (id.getPageCatag() == BPTPageId.ROOT_PTR) {
            parent = 0;
        } else {
            parent = id.getPageNumber();
        }
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

    public abstract int getNumEmptySlots();

    public abstract boolean isSlotUsed(int i );
}
