package cn.edu.thssdb.exception;

public class StringTooLongException extends Exception {
    @Override
    public String getMessage() {
        return "Exception: input string is too long.";
    }
}
