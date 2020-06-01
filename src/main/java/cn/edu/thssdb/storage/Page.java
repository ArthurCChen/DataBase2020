package cn.edu.thssdb.storage;

public interface Page {

    PageId getId();

    void markDirty(boolean dirty);

    boolean isDirty();

    byte[] getData();
}
