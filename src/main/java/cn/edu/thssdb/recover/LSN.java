package cn.edu.thssdb.recover;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

public class LSN
implements  Cloneable , Comparable<LSN>{

    private int fileId;

    private int fileOffset;

    private int recordSize;

    public LSN(int fileId, int fileOffset){
        this.fileId = fileId;
        this.fileOffset = fileOffset;
        this.recordSize  = 0;
    }

    @Override
    public String toString() {
        return String.format("LSN[%05d;%08d]", fileId, fileOffset);
    }

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public int getFileId() {
        return fileId;
    }

    public int getFileOffset() {
        return fileOffset;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  LSN){
            LSN lsn = (LSN) obj;
            return lsn.fileId == fileId && lsn.fileOffset == fileOffset;
        }
        return false;
    }



   public LSN clone(){
        try{
            LSN lsn = (LSN) super.clone();
            return lsn;
        }catch (CloneNotSupportedException e){
            throw new InternalException("lsn is not likely to cause a clone error");
        }
   }

    @Override
    public int compareTo(LSN o) {
        if(fileId != o.fileId)
            return fileId - o.fileId;
        return fileOffset - o.fileOffset;
    }
}
