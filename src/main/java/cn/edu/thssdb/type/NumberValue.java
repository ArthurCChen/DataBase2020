package cn.edu.thssdb.type;

public abstract class NumberValue implements ColumnValue{
    private static final long serialVersionUID = 1L;
    protected  final Number value;
    protected  final ColumnType type;

    public NumberValue(Number value, ColumnType type){
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
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
