package cn.edu.thssdb.storage;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.storage.operation.DeleteOperation;
import cn.edu.thssdb.storage.operation.FileOperation;
import cn.edu.thssdb.storage.operation.InsertOperation;
import cn.edu.thssdb.storage.operation.UpdateOperation;
import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BufferPool {
    private static int pageSize = Global.pageSize;
    private int chunkSize = Global.bufferChunkSize;

    private HashMap<PageId, Page> pageMap;
    private class ReplaceAlgorithm{
        // 使用clock算法
        private LinkedList<PageId> idQueue;
        private HashMap<PageId, Boolean> whetherVisited;

        ReplaceAlgorithm(){
            idQueue = new LinkedList<>();
            whetherVisited = new HashMap<>();
        }

        void addPage(PageId id){
            if(whetherVisited.containsKey(id)){
                this.whetherVisited.put(id, true);
            }else{
                idQueue.add(id);
                this.whetherVisited.put(id, false);
            }
        }

        PageId evict(){
            while(!idQueue.isEmpty()){
                PageId id = idQueue.removeFirst();
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

    public Page getPage(PageId id){
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
        PageId id = replaceAlgorithm.evict();
        if(pageMap.get(id).isDirty()){
            flushPage(id);
        }
        discardPage(id);
    }

    public void discardPage(PageId id){
        pageMap.remove(id);
    }

    private void flushPage(PageId id){
        Page page = this.pageMap.get(id);
        try{
            FileHandler file = Global.getFileFromPid(id);
            file.writePage(page);
        }catch(Exception e){

        }
        page.markDirty(false);

    }

    public void flushAllPages(){
        for(PageId pid: pageMap.keySet()){
            flushPage(pid);
        }
    }

    public boolean operateRow(Integer tid, Row row, FileOperation op){
        try {
            Table table = Global.getTableFromTid(tid);
            FileHandler file = table.getFileHandler();
            ArrayList<Page> dirtyPages = op.operate(file, row);
            for (Page page : dirtyPages) {
                page.markDirty(true);
                if (op instanceof InsertOperation) {
                    pageMap.put(page.getId(), page);
                }
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }


    public boolean insertRow(Integer tid, Row row){
        return operateRow(tid, row, new InsertOperation());
    }

    public boolean deleteRow(Integer tid, Row row){
        return operateRow(tid, row, new DeleteOperation());
    }

    public boolean updateRow(Integer tid, Row row) {
        return operateRow(tid, row, new UpdateOperation());
    }
}
