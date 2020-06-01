package cn.edu.thssdb.storage;

public interface PageId {

    int[] serialize();

    int getTableId();

    int hashCode();

    boolean equals(Object obj);

    int getPageNumber();
}
