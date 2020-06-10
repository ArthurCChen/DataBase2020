package cn.edu.thssdb.recover;

// TXN means transaction
public enum WALType {
    START_TXN(),
    UPDATE_TXN(),
    UPDATE_PAGE_REDO_ONLY(),
    COMMIT_TXN(),
    ABORT_TXN();
}
