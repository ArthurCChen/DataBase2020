// 记录table的信息,方便序列化
package cn.edu.thssdb.schema;

import java.io.Serializable;

public class TableInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public int count = 0;
    public int autoIncrement = 0;

    public TableInfo(){
        this(0,0);
    }

    public TableInfo(int count, int autoIncrement){
        this.count = count;
        this.autoIncrement = autoIncrement;
    }
}
