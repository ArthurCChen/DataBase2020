package cn.edu.thssdb.storage;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.storage.operation.DeleteOperation;
import cn.edu.thssdb.storage.operation.FileOperation;
import cn.edu.thssdb.storage.operation.InsertOperation;
import cn.edu.thssdb.storage.operation.UpdateOperation;
import cn.edu.thssdb.utils.Global;
import com.sun.rmi.rmid.ExecPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BufferPool {
    private static int pageSize = Global.pageSize;
    private int chunkSize = Global.bufferChunkSize;

    private HashMap<String, Page> pageMap;
    private class ReplaceAlgorithm{
        // 使用clock算法
        private LinkedList<String> idQueue;
        private HashMap<String, Boolean> whetherVisited;

        ReplaceAlgorithm(){
            idQueue = new LinkedList<>();
            whetherVisited = new HashMap<>();
        }

        void addPage(String id){
            if(whetherVisited.containsKey(id)){
                this.whetherVisited.put(id, true);
            }else{
                idQueue.add(id);
                this.whetherVisited.put(id, false);
            }
        }

        String evict(){
            while(!idQueue.isEmpty()){
                String id = idQueue.removeFirst();
                Boolean visited = whetherVisited.remove(id);
                if(visited){
                    idQueue.addLast(id);
                    whetherVisited.replace(id, false);
                } else{
                    return id;
                }
            }
            return null;
        }



    }

    private ReplaceAlgorithm replaceAlgorithm;

    public BufferPool(){
        this.pageMap = new HashMap<>();
        this.replaceAlgorithm = new ReplaceAlgorithm();
    }

    public BufferPool(int chunkSize){
        this();
        this.chunkSize = chunkSize;
    }

    public Page getPage(String id){
        Page page;
        if(pageMap.containsKey(id)){
            page = pageMap.get(id);
        }else{
            FileHandler file = Global.getFileFromPid(id);
            page = file.readPage(id);
            if(pageMap.size() >= this.chunkSize){
                evictPage();
            }
        }
        replaceAlgorithm.addPage(id);
        return page;
    }

    private void evictPage(){
        String id = replaceAlgorithm.evict();
        if(pageMap.get(id).isDirty()){
            flushPage(id);
        }
        discardPage(id);
    }

    public void discardPage(String id){
        pageMap.remove(id);
    }

    private void flushPage(String id){
        Page page = this.pageMap.get(id);
        try{
            FileHandler file = Global.getFileFromPid(id);
            file.writePage(page);
        }catch(Exception e){

        }
        page.markDirty(false);

    }

    public void operateRow(String tid, Row row, FileOperation op){
        Table table = Global.getTableFromTid(tid);
        FileHandler file = table.getFile();
        ArrayList<Page> dirtyPages = op.operate(file, row);
        for(Page page: dirtyPages){
            page.markDirty(true);
        }
    }


    public void insertRow(String tid, Row row){
        operateRow(tid, row, new InsertOperation());
    }

    public void deleteRow(String tid, Row row){
        operateRow(tid, row, new DeleteOperation());
    }

    public void updateRow(String tid, Row row) {
        operateRow(tid, row, new UpdateOperation());
    }
}
