package cn.edu.thssdb.exception;

public class ColumnRuleDiscrepancyException extends RuntimeException{
    @Override
    public String getMessage() {
        return "Exception: Column Rule Discrepancy!";
    }
}