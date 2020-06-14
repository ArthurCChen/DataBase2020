package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.exception.IndexException;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.RowDesc;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.storage.PageId;
import cn.edu.thssdb.utils.Global;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class HeapPage implements Page {
    final HeapPageId pid;
    final RowDesc td;
    final BitSet header;
    final Row rows[];
    final int numSlots;
    boolean dirty;

    public HeapPage(HeapPageId id, byte[] data, RowDesc td) throws IOException {
        this.pid = id;
        this.td = td;
        this.numSlots = getNumTuples();
        this.dirty = false;
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

        byte[] header_in = new byte[getHeaderSize()];
        if(dis.read(header_in, 0, header_in.length) < header_in.length)
            throw new IOException("pageSize is too small");
        header = BitSet.valueOf(header_in);

        rows = new Row[numSlots];
        try{
            for(short i =0; i < rows.length; i ++)
                rows[i] = readNextRow(dis, i);
        }catch(Exception e){
            e.printStackTrace();
        }
        dis.close();
    }
    @Override
    public PageId getId() {
        return pid;
    }

    private int getNumTuples() {
        return (int) Math.floor(
                (Global.pageSize * 8.0) / (this.td.getByteSize() * 8.0 + 1.0)
        );
    }

    public Row getRowByOffset(int offset){
        if(!isSlotUsed(offset)){
            throw new IndexException();
        }
        return this.rows[offset];
    }


    public boolean isSlotUsed(int i) {
        if(i < 0 || i >= this.numSlots){
            return false;
        }
        return this.header.get(i);
    }

    private Row readNextRow(DataInputStream dis, short slotId) throws Exception{
        if (!isSlotUsed(slotId)){

            for(int i = 0; i < td.getByteSize(); i ++){
                try{
                    dis.readByte();
                }catch(IOException e){
                    throw new Exception("no such row");
                }
            }
            return null;
        }

        ArrayList<String> attrs = new ArrayList<>();
        ArrayList<Object> vals = new ArrayList<>();
        for(int i = 0; i < td.getColumnSize(); i ++){
            attrs.add(td.getAttrNames().get(i));
            vals.add(td.get(i).parse(dis));
        }
        Row row = new Row(td, attrs, vals);
        row.setPageOffset(slotId);
        row.setPageId(pid);
        return row;
    }

    private int getHeaderSize(){
        return (int) Math.ceil(this.numSlots / 8.0);
    }

    @Override
    public void markDirty(boolean dirty) {
        this.dirty = dirty;
    }

    void paddingZero(DataOutputStream dos, int length) throws IOException{
        for (int i = 0 ; i < length; i ++)
            dos.write((byte)0);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public byte[] getData() {
        int len = Global.pageSize;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            // create the header of the page
            byte[] headBuffer = this.header.toByteArray();
            dos.write(headBuffer, 0, headBuffer.length);
            paddingZero(dos, getHeaderSize() - headBuffer.length);
            // create the tuples
            int tupleSize = td.getByteSize();
            for (int i=0; i< rows.length; i++) {
                // empty slot
                if (!isSlotUsed(i)) {
                    paddingZero(dos, tupleSize);
                } else {// non-empty slot
                    rows[i].serialize(dos);
                }
            }

            // padding
            int zerolen = Global.pageSize - (headBuffer.length + td.getByteSize() * rows.length); //- numSlots * td.getSize();
            paddingZero(dos, zerolen);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    public Iterator<Row> iterator() {
        ArrayList<Row> tupleArrayList = new ArrayList<>();
        for(int i=0; i<numSlots; i++){
            if(isSlotUsed(i)){
                tupleArrayList.add(rows[i]);
            }
        }
        return tupleArrayList.iterator();
    }

    public static byte[] createEmptyPageData() {
        int len = Global.pageSize;
        return new byte[len]; //all 0
    }

    public void updateRow(Row t){
        int tupleNumber = t.getPageOffset();

        if (t.getPageId() != pid) {
            throw new InternalException("pid not match");
        } else if (!isSlotUsed(tupleNumber)) {
            throw new InternalException("slot not in use");
        } else {
            rows[tupleNumber] = t;
        }
    }

    public void deleteRow(Row t){
        int tupleNumber = t.getPageOffset();

        if (t.getPageId() != pid) {
            throw new InternalException("pid not match");
        } else if (!isSlotUsed(tupleNumber)) {
            throw new InternalException("slot not in use");
        } else {
            rows[tupleNumber] = null;
            markSlotUsed(tupleNumber, false);
        }
    }

    private void markSlotUsed(int i, boolean value) {
        this.header.set(i, value);
    }
    public int getNumEmptySlots() {
        return this.numSlots - this.header.cardinality();
    }

    public void insertRow(Row t){
        if (this.getNumEmptySlots() == 0) {
            throw new InternalException("page is full");
        }
        if (!this.td.equals(t.getRowDesc())) {
            throw new InternalException("tupledesc is mismatch");
        }
        short i = this.nextEmptySlotNum();
        rows[i] = t;
        markSlotUsed(i, true);
        t.setPageOffset(i);
        t.setPageId(pid);
    }



    public short nextEmptySlotNum() {
        for (short i=0; i<this.numSlots; i++) {
            if (!isSlotUsed(i)){
                return i;
            }
        }
        throw new InternalException("no nextEmptySlotNum available");
    }

}
