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
        String info = new String("Exception for:");
        if (satisfy(PRIMARY, cause))
            info +=  "insert row not satisfy the constraint; ";
        if ( satisfy(ISNOTNULL, cause))
            info += "isnotnull not satisfied ; ";

        return info;
    }
}
