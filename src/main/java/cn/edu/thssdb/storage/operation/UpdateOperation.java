package cn.edu.thssdb.storage.operation;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.storage.FileHandler;
import cn.edu.thssdb.storage.Page;

import java.util.ArrayList;

public class UpdateOperation implements FileOperation {
    public ArrayList<Page> operate(FileHandler file, Row row){
        return file.updateRow(row);
    }
}
