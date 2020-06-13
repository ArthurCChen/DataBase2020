package cn.edu.thssdb.storage;

public class BufferException  extends RuntimeException{
    String cause;

    public BufferException(String cause){
        this.cause = cause;
    }


    @Override
    public String getMessage() {
        return String.format("Exception: buffer overflow because : %s!", cause);
    }
}