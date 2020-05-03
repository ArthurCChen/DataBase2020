package cn.edu.thssdb.type;

public interface ValueFactory {
    public ColumnValue getField(ColumnType type, byte[] data){
        switch(type){
            case STRING:
                return new StringValue();
            case LONG:
                return ((Number) value).longValue();
            case FLOAT:
                return ((Number)value).floatValue();
            case DOUBLE:
                return ((Number)value).doubleValue();
            case INT:
                return ((Number)value).intValue();
        }
    }
}
