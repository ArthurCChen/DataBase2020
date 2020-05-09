package cn.edu.thssdb.exception;

public class ColumnNumDiscrepancyException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Exception: Column Num Discrepancy!";
    }
}