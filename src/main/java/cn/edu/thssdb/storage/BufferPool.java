package cn.edu.thssdb.storage;

import cn.edu.thssdb.exception.BufferException;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.storage.operation.DeleteOperation;
import cn.edu.thssdb.storage.operation.FileOperation;
import cn.edu.thssdb.storage.operation.InsertOperation;
import cn.edu.thssdb.storage.operation.UpdateOperation;
import cn.edu.thssdb.utils.Global;

import java.util.*;

public class BufferPool {
    private static int pageSize = Global.pageSize;
    private int chunkSize = Global.bufferChunkSize;

    private HashMap<PageId, Page> pageMap;
    private HashMap<Integer, HashSet<PageId>> tablesDirtyPages; // 用于保存当前table所有的脏页
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
            int queueSize = idQueue.size();
            int cnt = 0;
            for( ; !idQueue.isEmpty(); cnt ++){
                // 使用dirty来当作pinned与否的标志
                if(pageMap.get(idQueue.getFirst()).isDirty()) {
                    //TODO: 当dirty的过多时,会发生无穷循环
                    if(cnt > queueSize * 2){
                        throw new BufferException("all buffer is pinned");
                    }
                    continue;
                }

                PageId id = idQueue.removeFirst();
                Boolean visited = whetherVisited.remove(id);
                if(!pageMap.containsKey(id))
                    continue;

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
        this.tablesDirtyPages = new HashMap<>();
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
//        throw new InternalException("evict page not supported");
        PageId id = replaceAlgorithm.evict();
        if(pageMap.get(id).isDirty()){
            flushPage(id);
        }
        discardPage(id);
    }

    public void discardPage(PageId id){
        Page page = pageMap.get(id);
        if(page.isDirty()){
            unMarkPageDirty(page);
        }
        pageMap.remove(id);
    }

    private void flushPage(PageId id){
        Page page = this.pageMap.get(id);
        try{
            FileHandler file = Global.getFileFromPid(id);
            file.writePage(page);
        }catch(Exception e){

        }
        unMarkPageDirty(page);

    }

    public void flushAllPages(){
        for(PageId pid: pageMap.keySet()){
            flushPage(pid);
        }
    }

    public void flushPagesOfTable(int tid){
        HashSet<PageId> pageIds = tablesDirtyPages.getOrDefault(tid, null);
        if (pageIds != null){
            HashSet<PageId> clone = (HashSet<PageId>) pageIds.clone();
            for(PageId pid: clone){
                flushPage(pid);
            }
        }
        tablesDirtyPages.remove(tid);
    }

    public void discardPagesOfTables(int tid){
        HashSet<PageId> pageIds = tablesDirtyPages.getOrDefault(tid, null);
        if (pageIds != null){
            HashSet<PageId> clone = (HashSet<PageId>) pageIds.clone();
            for(PageId pid: clone){
                discardPage(pid);
            }
        }
        tablesDirtyPages.remove(tid);
    }


    private void markPageDirty(Page page){
        page.markDirty(true);
        PageId pageId = page.getId();
        int tid = pageId.getTableId();
        if(! this.tablesDirtyPages.containsKey(tid)){
            this.tablesDirtyPages.put(tid, new HashSet<PageId>());
        }
        HashSet<PageId> pageIds = tablesDirtyPages.get(tid);
        pageIds.add(pageId);
    }

    private void unMarkPageDirty(Page page){
        page.markDirty(false);
        PageId pageId = page.getId();
        int tid = pageId.getTableId();
        HashSet<PageId> pageIds = tablesDirtyPages.get(tid);
        pageIds.remove(pageId);
    }

    public boolean operateRow(Integer tid, Row row, FileOperation op){
        try {
            Table table = Global.getTableFromTid(tid);
            FileHandler file = table.getFileHandler();
            ArrayList<Page> dirtyPages = op.operate(file, row);
            for (Page page : dirtyPages) {
                markPageDirty(page);
                if (op instanceof InsertOperation) {
                    pageMap.put(page.getId(), page);
                }
            }
            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
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
