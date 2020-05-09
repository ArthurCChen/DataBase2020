package cn.edu.thssdb.exception;


public class ColumnTypeDiscrepancyException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Exception: Column Type Discrepancy!";
    }
}