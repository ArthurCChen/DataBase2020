package cn.edu.thssdb.exception;

public class DeserializeException extends RuntimeException{
    private String filename;
    DeserializeException(String filename){
        this.filename = filename;
    }
    @Override
    public String getMessage() {
        return "Exception: cannot deserialize file" + filename;
    }
}