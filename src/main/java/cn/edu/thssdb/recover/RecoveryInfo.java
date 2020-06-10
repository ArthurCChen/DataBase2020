package cn.edu.thssdb.recover;

import java.util.HashMap;

public class RecoveryInfo {
    public LSN firstLsn;
    public LSN nextLsn;

    public int maxTxnId;
    public HashMap<Integer, LSN> incompleteTxns;

    public RecoveryInfo(LSN firstLsn,
                        LSN nextLsn){
        this.firstLsn = firstLsn;
        this.nextLsn = nextLsn;

        this.maxTxnId = -1;

        incompleteTxns = new HashMap<>();
    }

    public void updateInfo(int txnId, LSN lsn){
        incompleteTxns.put(txnId, lsn);

        maxTxnId = txnId > maxTxnId? txnId : maxTxnId;
    }

    public LSN getLastLsn(int txnId){
        return incompleteTxns.get(txnId);
    }

    public void completeTxn(int txnId){
        incompleteTxns.remove(txnId);
    }

    public boolean hasIncompleteTxn(){
        return !incompleteTxns.isEmpty();
    }

    public boolean isTxnCompleted(int txnId){
        return incompleteTxns.containsKey(txnId);
    }
}
