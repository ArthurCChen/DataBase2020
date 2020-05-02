package cn.edu.thssdb.exception;

public class FileNotExistException extends RuntimeException{

    private String foldername;
    private String filename;

    public FileNotExistException(String foldername, String filename){
        super();
        this.foldername = foldername;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getFoldername() {
        return foldername;
    }

    @Override
    public String getMessage() {
        return "Exception: insertion caused duplicated keys!";
    }

}
