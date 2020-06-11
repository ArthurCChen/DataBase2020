package cn.edu.thssdb.recover;

import cn.edu.thssdb.storage.BufferPool;
import cn.edu.thssdb.storage.FileHandler;
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

    private String databasePath;

    private String tableName;

    private BufferPool buffer;

    private LSN firstLsn;

    private LSN nextLsn;

    public WALManager(
            String path,
            String databaseName,
            BufferPool buffer){
        this.buffer = buffer;
        this.databasePath = path;
        this.tableName = databaseName;
    }

    public File createWALFile(int fileId){
        String filename = getWALfileName(tableName, fileId);
        File diskFile = new File(
                Global.synthFilePath(databasePath, filename));
        try {
            diskFile.createNewFile();
            return diskFile;
        }catch (Exception e){
            throw new InternalException(String.format("could not create file %s", filename));
        }
    }

    public File openWALFile(int fileId){
        String filename = getWALfileName(tableName, fileId);
        File diskFile = new File(
                Global.synthFilePath(databasePath, filename));
        if (diskFile.exists()){
            return diskFile;
        }else{
            return createWALFile(fileId);
        }
    }

    public RecoveryInfo recover(LSN storedFirstLsn, LSN storedNextLsn){
        firstLsn = storedFirstLsn;
        nextLsn = storedNextLsn;

        RecoveryInfo recoveryInfo = new RecoveryInfo(firstLsn, nextLsn);


    }
}
