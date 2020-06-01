package cn.edu.thssdb.type;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

public class ValueFactory {
    public static ColumnValue getValue(Object value){
        if(value instanceof  ColumnValue)
            return (ColumnValue)value;
        else if(value instanceof Integer){
            return new IntValue((int)value);
        }else if(value instanceof Long){
            return new LongValue((Long) value);
        }else if(value instanceof Float){
            return new FloatValue((Float) value);
        }else if(value instanceof Double){
            return new DoubleValue((Double) value);
        }else if(value instanceof String){
            return new StringValue((String) value, ((String)value).length());
        }else{
            throw new InternalException("Not implemented");
        }
    }

    public static ColumnValue getNullValue(ColumnType type){
        switch (type){
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return new IntValue(0, false);
            case STRING:
                return new StringValue("", 0, false);
            default:
                throw new InternalException("not implemented");
        }
    }


    public static ColumnValue getValue(Object value, ColumnType type, int maxLen){
        if(value instanceof ColumnValue){
            ColumnValue colVal = (ColumnValue) value;
            switch (colVal.getType()){
                case STRING:
                    return new StringValue((String)colVal.getValue(), maxLen);
                case DOUBLE:case FLOAT:case LONG:case INT:
                    return colVal;
            }
        }
        switch (type){
            case INT:
                if(value instanceof  Long){
                    return new IntValue(((Long)value).intValue());
                }else if(value instanceof Integer){
                    return getValue(value);
                }else{
                    throw new InternalException("cast error");
                }
            case LONG:
                if(value instanceof Long){
                    return getValue(value);
                }else if(value instanceof Integer){
                    return new LongValue(((Integer)value).longValue());
                }else{
                    throw new InternalException("cast error");
                }
            case DOUBLE:
                if(value instanceof Double){
                    return getValue(value);
                }else if(value instanceof Float){
                    return new DoubleValue(((Float)value).doubleValue());
                }else{
                    throw new InternalException("cast error");
                }
            case FLOAT:
                if(value instanceof Float){
                    return getValue(value);
                }else if(value instanceof Double){
                    return new FloatValue(((Double)value).floatValue());
                }else{
                    throw new InternalException("cast error");
                }
            case STRING:
                if(value instanceof String){
                    return new StringValue((String)value, maxLen);
                }else{
                    throw new InternalException("cast error");
                }
            default:
                throw new InternalException("not implemented");
        }
    }
}
