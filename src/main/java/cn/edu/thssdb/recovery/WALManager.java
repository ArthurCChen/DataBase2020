package cn.edu.thssdb.recovery;
// WAL manager 在此只需要管理b加树索引的更新
import cn.edu.thssdb.storage.BufferPool;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.Page;
import cn.edu.thssdb.utils.Global;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.io.File;

public class WALManager {

    public static final int MAX_WAL_FILE_SIZE = 1024*1024*10;

    public static final int OFFSET_PREV_FILE_END = 2;

    public static final int OFFSET_FIRST_RECORD = 6;

    public static String getWALfileName(
            String databaseName,
            int fileId){
        return String.format(Global.LOG_FORMAT, databaseName, String.valueOf(fileId));
    }



}
