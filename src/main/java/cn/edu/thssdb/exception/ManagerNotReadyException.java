package cn.edu.thssdb.exception;

public class ManagerNotReadyException extends Exception {
    @Override
    public String getMessage() {
        return "Exception: The query manager is not ready yet.";
    }
}
