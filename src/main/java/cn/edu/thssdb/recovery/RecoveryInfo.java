package cn.edu.thssdb.recovery;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class RecoveryInfo implements Serializable {

    public int txnId;
    public WALType walType;
    //public int fileId; 每个table单独处理
    public int pageNum;
    public short pageOffset;

    public RecoveryInfo(int txnId, WALType walType, int pageNum, short pageOffset){
        this.txnId = txnId;
        this.walType = walType;
        this.pageNum = pageNum;
        this.pageOffset = pageOffset;
    }

    void serialize(DataOutputStream dos) throws IOException {
        dos.writeInt(txnId);
        dos.writeInt(walType.id);
        dos.writeInt(pageNum);
        dos.writeInt(pageOffset);
    }

    static public RecoveryInfo parse(DataInputStream dis) throws IOException{
        int txnId = dis.readInt();
        int walTypeId = dis.readInt();
        int pageNum = dis.readInt();
        short pageOffset = dis.readShort();
        WALType walType = WALType.getTypeFromId(walTypeId);
        return new RecoveryInfo(txnId, walType, pageNum, pageOffset);
    }
}
