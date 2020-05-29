package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.TableMeta;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.utils.Global;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.BitSet;

public class HeapPage implements Page {
    final HeapPageId pid;
    final TableMeta td;
    final BitSet header;
    final Row rows[];
    final int numSlots;
    boolean dirty;

    public HeapPage(HeapPageId id, byte[] data, TableMeta td) throws IOException {
        this.pid = id;
        this.td = td;
        this.numSlots = getNumTuples();
        this.dirty = false;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

        byte[] header_in = new byte[getHeaderSize()];
        for(int i = 0; i < header_in.length; i ++)
            header_in[i] = dis.readByte();
        header = BitSet.valueOf(header_in);

        rows = new Row[numSlots];
        try{
            for(int i =0; i < rows.length; i ++)
                rows[i] = readNextRow(dis, i);
        }catch(Exception e){

        }
        dis.close();
    }
    @Override
    public PageId getId() {
        return null;
    }

    private int getNumTuples() {
        return (int) Math.floor(
                (Global.pageSize * 8.0) / (this.td.getByteSize() * 8 + 1)
        );
    }

    public boolean isSlotUsed(int i) {
        if(i < 0 || i >= this.numSlots){
            return false;
        }
        return this.header.get(i);
    }

    private Row readNextRow(DataInputStream dis, int slotId) throws Exception{
        if (!isSlotUsed(slotId)){
            for(int i = 0; i < td.getByteSize(); i ++){
                try{
                    dis.readByte();
                }catch(IOException e){

                }
            }
            return null;
        }

        Row row = new Row(td);
    }

    private int getHeaderSize(){
        return (int) Math.ceil(this.numSlots / 8.0);
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
