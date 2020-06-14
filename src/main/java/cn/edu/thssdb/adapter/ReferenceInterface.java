package cn.edu.thssdb.adapter;

import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.storage.FileIterator;
import cn.edu.thssdb.utils.Physical2LogicalInterface;

import java.util.ArrayList;

// TODO:这个实现用来展现如何使用
// TODO:实现事务
public class ReferenceInterface implements Physical2LogicalInterface {
    static Manager manager = null;
    boolean useTestDatabase = false;
    static ReferenceInterface INSTANCE;

    public Manager getManager() {
        return manager;
    }

    private ReferenceInterface() {
        // 获取Manager单例
        manager = Manager.getInstance();
        //  尝试读取Manager内的元数据,若不存在,也无事发生
       try {
           manager.recover();
           // 创建一个名为test的Database
           manager.createDatabase("test");
           // 将Manager当前指向的Database调整为test

//        //
//        manager.getCurrentDatabase().drop("test");
       }catch (Exception e){
//           e.printStackTrace();
       }
        manager.useDatabase("test");
    }

    static public ReferenceInterface getInstance(){
        if(manager == null){
            INSTANCE = new ReferenceInterface();
        }
        return INSTANCE;
    }
    /**
    * @param columns table的列信息

    * */
    @Override
    public boolean create_table(String table_name, ArrayList<Column> columns, int transaction_id) {
        try{
            manager.getCurrentDatabase().create(table_name, columns);
//            manager.persistMeta();
            return true;
        }catch (Exception e){
            return false;
        }
    }


    @Override
    public boolean drop_table(String table_name, int transaction_id) {
        try {
            manager.getCurrentDatabase().drop(table_name);
//            manager.persistMeta();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public LogicalTable get_table(String table_name, int transaction_id) {
        Table realTable = manager.getCurrentDatabase().getTable(table_name);
//        realTable.txnId = transaction_id;
        if(realTable != null) {
//            LogicalTable logicalTable = new HeapTable(realTable);
            LogicalTable logicalTable = realTable;
            return logicalTable;
        }else{
            return null;
        }
    }

    /**
     *
     * @param table_name 表名称
     * @param row 添加的列
     * @param transaction_id
     * @return
     */
    @Override
    public boolean insert_row(String table_name, Row row, int transaction_id) {

        Table table = manager.getCurrentDatabase().getTable(table_name);
        return table.insertRow(row);

    }

    /**
     *
     * @param table_name
     * @param primary_key  主键,不推荐使用
     * @param transaction_id
     * @return
     */
    @Override
    public boolean delete_row(String table_name, Entry primary_key, int transaction_id) {
        try{
            Table table = manager.getCurrentDatabase().getTable(table_name);
            Boolean success = false;
            // 通过iterator遍历,来完成删除对应的操作
            FileIterator iter = table.getIterator();
            ArrayList<Row> rows = new ArrayList<>();
            while(iter.hasNext()){
                Row row = iter.next();
                if(row.matchValue(table.getTableMeta().getPrimaryNames().get(0), primary_key.value)){
                    //table调用index在文件中删去row
                    success = table.deleteRow(row);
                }
            }
            iter.close();

            return success;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public Row search(String table_name, Entry primary_key, int transaction_id) {
        try{
            Table table = manager.getCurrentDatabase().getTable(table_name);

            // 通过iterator遍历,来完成删除对应的操作
            FileIterator iter = table.getIterator();
            ArrayList<Row> rows = new ArrayList<>();
            while(iter.hasNext()){
                Row row = iter.next();
                if(row.matchValue(table.getTableMeta().getPrimaryNames().get(0), primary_key.value)){
                    //table调用index在文件中删去row
                    return row;
                }
            }
            iter.close();
        }catch (Exception e){
            return null;
        }finally {
            return null;
        }
    }

    @Override
    public int start_transaction() {
        return 0;
    }

    @Override
    public boolean abort(int transaction_id) {
        return false;
    }

    @Override
    public boolean commit(int transaction_id) {
        return false;
    }
}
