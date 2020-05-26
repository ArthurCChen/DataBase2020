package cn.edu.thssdb.schema;

import cn.edu.thssdb.predicate.base.Predicate;

import java.util.ArrayList;

/**
 * This is a virtual table to for A join B on ...
 */
public class VirtualTable {
    public ArrayList<String> tables = new ArrayList<>();
    public Predicate condition;

    @Override
    public String toString() {
        return String.format("VirtualTable: {tables: %s, condition: %s}", tables, condition);
    }
}
