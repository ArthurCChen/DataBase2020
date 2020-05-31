package cn.edu.thssdb.utils;

import cn.edu.thssdb.schema.*;

import java.io.IOException;
import java.util.ArrayList;

import cn.edu.thssdb.storage.FileIterator;
import com.sun.*;
// TODO:这个实现用来展现如何使用
// TODO:实现事务
public class ReferenceInterface implements Physical2LogicalInterface{
    Manager manager;
    ReferenceInterface() throws Exception {
        // 获取Manager单例
        manager = Manager.getInstance();
        //  尝试读取Manager内的元数据,若不存在,也无事发生
        manager.recover();
        // 创建一个名为test的Database
        manager.createDatabase("test");
        // 将Manager当前指向的Database调整为test
        manager.useDatabase("test");
//        //
//        manager.getCurrentDatabase().drop("test");

    }
    /**
    * @param columns table的列信息
     * @param primaryNames primary键对应的属性名
    * */
    @Override
    public boolean create_table(String table_name, ArrayList<Column> columns,  ArrayList<String> primaryNames, int transaction_id) {
        try{
            assert(primaryNames.size() == 1);// 暂时只支持一个主键
            manager.getCurrentDatabase().create(table_name, columns, primaryNames);
            return true;
        }catch (Exception e){
            return false;
        }
    }


    @Override
    public boolean drop_table(String table_name, int transaction_id) {
        try {
            manager.getCurrentDatabase().drop(table_name);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public Table get_table(String table_name, int transaction_id) {
        return manager.getCurrentDatabase().getTable(table_name);
    }

    /**
     *
     * @param table_name 表名称
     * @param attrNames  非空属性名称
     * @param values     与上面对应的非空值
     * @param transaction_id
     * @return
     */
    @Override
    public boolean insert_row(String table_name, ArrayList<String> attrNames, ArrayList<Object> values, int transaction_id) {
        try{
            Table table = manager.getCurrentDatabase().getTable(table_name);
            table.insertRow(attrNames, values);
            return true;
        }catch (Exception e){
            return false;
        }
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

            // 通过iterator遍历,来完成删除对应的操作
            FileIterator iter = table.iterator();
            ArrayList<Row> rows = new ArrayList<>();
            while(iter.hasNext()){
                Row row = iter.next();
                if(row.matchValue(table.getTableMeta().getPrimaryNames().get(0), primary_key.value)){
                    //table调用index在文件中删去row
                    table.deleteRow(row);
                }
            }
            iter.close();

            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public Row search(String table_name, Entry primary_key, int transaction_id) {
        try{
            Table table = manager.getCurrentDatabase().getTable(table_name);

            // 通过iterator遍历,来完成删除对应的操作
            FileIterator iter = table.iterator();
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
