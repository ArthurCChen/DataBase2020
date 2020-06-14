package cn.edu.thssdb.recovery;

// TXN means transaction
public enum WALType {
    START_TXN(0),//开始事务
    INSERT_ROW(1),//添加行
    DELETE_ROW(2),//删除行
    COMMIT_TXN(3),//提交事务
    ABORT_TXN(4),//放弃事务
    CHECKPOINT(5); // checkpoint 暂时未实现

    int id;
    WALType(int id){
        this.id = id;
    }



    public static WALType getTypeFromId(int id){
        switch(id){
            case 0:return START_TXN;
            case 1: return INSERT_ROW;
            case 2: return DELETE_ROW;
            case 3: return COMMIT_TXN;
            case 4: return ABORT_TXN;
        }
        return null;
    }
}
