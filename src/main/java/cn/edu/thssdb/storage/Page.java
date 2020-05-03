package cn.edu.thssdb.storage;

public interface Page {

    String getId();

    void markDirty(boolean dirty);

    boolean isDirty();

    byte[] getData();
}
