package cn.edu.thssdb.type;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

public class ValueFactory {
    public static ColumnValue getField(Object value){
        if(value instanceof Integer){
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
            throw new InternalException("");
        }
    }

    public static ColumnValue getField(Object value, ColumnType type, int maxLen){
        switch (type){
            case INT:
                if(value instanceof  Long){
                    return new IntValue(((Long)value).intValue());
                }else if(value instanceof Integer){
                    return getField(value);
                }else{
                    throw new InternalException("");
                }
            case LONG:
                if(value instanceof Long){
                    return getField(value);
                }else if(value instanceof Integer){
                    return new LongValue(((Integer)value).longValue());
                }else{
                    throw new InternalException("");
                }
            case DOUBLE:
                if(value instanceof Double){
                    return getField(value);
                }else if(value instanceof Float){
                    return new DoubleValue(((Float)value).doubleValue());
                }else{
                    throw new InternalException("");
                }
            case FLOAT:
                if(value instanceof Float){
                    return getField(value);
                }else if(value instanceof Double){
                    return new FloatValue(((Double)value).floatValue());
                }else{
                    throw new InternalException("");
                }
            case STRING:
                if(value instanceof String){
                    return new StringValue((String)value, maxLen);
                }else{
                    throw new InternalException("");
                }
            default:
                throw new InternalException("");
        }
    }
}
