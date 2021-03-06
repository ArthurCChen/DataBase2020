package cn.edu.thssdb.recovery;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class RecoveryInfo implements Serializable {

    public int txnId;
    public WALType walType;
    public int tableId; //每个table单独处理
    public int pageNum;
    public short pageOffset;

    static private short defaultOffset = 0;

    public RecoveryInfo(int txnId, WALType walType){
        this(txnId,  walType,0,0,defaultOffset);
    }


    public RecoveryInfo(int txnId, WALType walType, int tableId,  int pageNum, short pageOffset){
        this.txnId = txnId;
        this.walType = walType;
        this.tableId = tableId;
        this.pageNum = pageNum;
        this.pageOffset = pageOffset;
    }

    void serialize(DataOutputStream dos) throws IOException {
        dos.writeInt(txnId);
        dos.writeInt(walType.id);
        dos.writeInt(tableId);
        dos.writeInt(pageNum);
        dos.writeInt(pageOffset);
    }

    static public RecoveryInfo parse(DataInputStream dis) throws IOException{
        int txnId = dis.readInt();
        int walTypeId = dis.readInt();
        int tableId = dis.readInt();
        int pageNum = dis.readInt();
        short pageOffset = dis.readShort();
        WALType walType = WALType.getTypeFromId(walTypeId);
        return new RecoveryInfo(txnId, walType, tableId, pageNum, pageOffset);
    }
}
