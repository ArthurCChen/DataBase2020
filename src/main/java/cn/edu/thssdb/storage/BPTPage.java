package cn.edu.thssdb.storage;

public class BPTPage implements Page {
    protected boolean dirty = false;

    protected final String id;

    BPTPage(String id){
        this.id = id;
    }


    @Override
    public String getId() {
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
