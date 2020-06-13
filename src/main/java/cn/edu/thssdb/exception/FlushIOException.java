package cn.edu.thssdb.exception;

public class FlushIOException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Exception: flush meets IOException";
    }
}