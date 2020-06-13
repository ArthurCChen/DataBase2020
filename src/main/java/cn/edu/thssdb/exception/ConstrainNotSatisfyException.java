package cn.edu.thssdb.exception;

public class ConstrainNotSatisfyException extends RuntimeException{
    public static int PRIMARY = 1;
    public static int ISNOTNULL = 3;
    private int cause;

    public ConstrainNotSatisfyException(int cause){
        this.cause = cause;
    }

    private boolean satisfy(int option, int cause){
        if((cause & option) == option)
            return true;
        return false;
    }

    @Override
    public String getMessage() {
        if (satisfy(PRIMARY, cause))
            return "Exception: insert row not satisfy the constraint: " + cause;
        return null;
    }
}
