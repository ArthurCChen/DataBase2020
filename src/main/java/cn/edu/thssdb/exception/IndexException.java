package cn.edu.thssdb.exception;

public class IndexException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Exception: index point to wrong place";
    }
}
