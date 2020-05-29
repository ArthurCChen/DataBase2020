package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.utils.Global;

import java.util.Iterator;

public class HeapFileIterator implements FileIterator {
    private Iterator<Row> iterator;
    private int pgNum;
    private HeapFile f;

    public HeapFileIterator(HeapFile f) {
        this.f = f;
    }


    @Override
    public void open(){
        pgNum = 0;
        iterator = getTupleIteratorFromPage(0);
    }

    /** @return true if there are more tuples available. */
    @Override
    public boolean hasNext(){
        if(iterator == null){
            return false;
        } else if (iterator.hasNext()) { // there are tuples available on pages
            return true;
        } else if (pgNum < f.numPages() - 1) { // there are more pages to iterate
            Iterator<Row> it = getTupleIteratorFromPage(pgNum+1);
            return it.hasNext();
        } else { // there are no more pages
            return false;
        }
    }

    @Override
    public Row next() {
        if (iterator == null) {
            return null;
        }

        if (iterator.hasNext()) { // there are tuples available on pages
            Row tuple = iterator.next();
            return tuple;
        } else if( pgNum < (f.numPages() - 1) ) { // there are more pages to iterate
            iterator = getTupleIteratorFromPage((++pgNum));
            if(iterator.hasNext()){
                return iterator.next();
            } else {
                return null;
            }
        } else { // there are no more pages
            return null;
        }
    }

    @Override
    public void rewind() {
        close();
        open();
    }

    /**
     * Closes the iterator.
     */
    @Override
    public void close(){
        iterator = null;
    }


    private Iterator<Row> getTupleIteratorFromPage(int pgNum){
        PageId pageId = new HeapPageId(f.getId(), pgNum);
        Page page = Global.gBufferPool().getPage(pageId);
        HeapPage heapPage = (HeapPage)page;
        return heapPage.iterator();
    }
}
