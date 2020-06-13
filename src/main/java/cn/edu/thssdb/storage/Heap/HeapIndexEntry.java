//用于索引文件的读写
package cn.edu.thssdb.storage.Heap;

import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ColumnValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class HeapIndexEntry implements Serializable {
    ColumnValue primary;
    int pageNumber;// page 所属在第几页
    int offset;

    public HeapIndexEntry(ColumnValue primary, int pid, int offset){
        this.primary = primary;
        this.pageNumber = pid;
        this.offset = offset;
    }

    void serialize(DataOutputStream dos) throws IOException {
        primary.serialize(dos);
        dos.writeInt(pageNumber);
        dos.writeInt(offset);
    }

    public static HeapIndexEntry parse(DataInputStream dis, ColumnType primaryType, int maxLen) throws IOException {
        ColumnValue val = primaryType.parse(dis, maxLen);
        int pageNumber = dis.readInt();
        int offset = dis.readInt();
        return new HeapIndexEntry(val, pageNumber, offset);
    }
}
