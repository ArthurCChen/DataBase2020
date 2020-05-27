package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.TableMeta;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.utils.Global;

import java.io.IOException;
import java.util.BitSet;

public class HeapPage implements Page {
    final HeapPageId pid;
    final TableMeta td;
    final BitSet header;
    final Row rows[];
    final int numSLots;
    boolean dirty;

    public HeapPage(HeapPageId id, byte[] data, TableMeta td) throws IOException {
        this.pid = id;
        this.td = td;
        this.numSLots = getNumTuples();
    }
    @Override
    public PageId getId() {
        return null;
    }

    private int getNumTuples() {
        return (int) Math.floor(
                (Global.pageSize * 8) / (this.td.size * 8 + 1)
        );
    }

    @Override
    public void markDirty(boolean dirty) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
