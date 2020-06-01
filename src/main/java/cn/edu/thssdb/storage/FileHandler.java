package cn.edu.thssdb.storage;

import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.schema.*;

import java.util.ArrayList;

public interface FileHandler {
    Page readPage(PageId id);

    void writePage(Page p);

    ArrayList<Page> insertRow(Row row) throws  Exception;

    ArrayList<Page> deleteRow(Row row) throws  Exception;

    ArrayList<Page> updateRow(Row row) throws  Exception;

    int getId();

    FileIterator iterator();

    RowDesc getMeta();
}
