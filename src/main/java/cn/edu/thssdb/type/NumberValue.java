package cn.edu.thssdb.type;

import cn.edu.thssdb.utils.Global;

public abstract class NumberValue implements ColumnValue{
    private static final long serialVersionUID = 1L;
    protected  final Number value;
    protected  final ColumnType type;
    protected  final boolean isNotNull;

    public NumberValue(Number value, ColumnType type){
        this(value, type, true);
    }

    public NumberValue(Number value, ColumnType type, Boolean isNotNull){
        this.value = value;
        this.type = type;
        this.isNotNull = isNotNull;
    }

    @Override
    public String toString() {
        if(! isNotNull)
            return Global.NULL_VALUE_DISPLAY;
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public abstract boolean greater_than(ColumnValue val);

    public abstract boolean less_than(ColumnValue val);

    public ColumnType getType() {
        return type;}
}
