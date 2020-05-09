package cn.edu.thssdb.index;

import cn.edu.thssdb.exception.*;
import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

public class TableTest {
    private Table table;

    @BeforeClass
    public void tableTestPrepare() {
        System.out.println("******** Table Testing ********");
    }

    @Before
    /*
    * 利用@Before在每个test类运行前都会运行的特点
    * 预置数据
     */
    public void dataPrepare() {
        System.out.println("Data Preparing ...");

        Column[] columns = new Column[2];
        columns[0] = new Column("ID", ColumnType.INT, 1, true, 10);
        columns[1] = new Column("name", ColumnType.STRING, 0, true, 50);
        columns[2] = new Column("dept_name", ColumnType.STRING, 0, true, 50);

        try {
            this.table = new Table("database", "table", columns);
        }
        catch (java.io.FileNotFoundException e) {
            //TODO emmm不知道do啥先放着
        }
    }

    @Test
    public void testInsert() {
        System.out.println("Insert Testing ...");

        Row testRow = new Row();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(1));
        entries.add(new Entry("CYN"));
        entries.add(new Entry("history"));
        testRow.appendEntries(entries);
        this.table.insert(testRow);

        testRow = new Row();
        entries = new ArrayList<Entry>();
        entries.add(new Entry(2));
        entries.add(new Entry("LSY"));
        entries.add(new Entry("software"));
        testRow.appendEntries(entries);
        this.table.insert(testRow);

        // 重复主键
        Assert.assertThrows(DuplicateKeyException.class, () -> {
            Row errorRow1 = new Row();
            ArrayList<Entry> errorEntries = new ArrayList<Entry>();
            errorEntries.add(new Entry(2));
            errorEntries.add(new Entry("Name Duplication"));
            errorEntries.add(new Entry("Dept_name Duplication"));
            errorRow1.appendEntries(errorEntries);
            this.table.insert(errorRow1);
        });

        // 行的属性个数与列不匹配
        Assert.assertThrows(ColumnNumDiscrepancyException.class, () -> {
            Row errorRow2 = new Row();
            ArrayList<Entry> errorEntries = new ArrayList<Entry>();
            errorEntries.add(new Entry(3));
            errorEntries.add(new Entry("3rd Column Loss"));
            errorRow2.appendEntries(errorEntries);
            this.table.insert(errorRow2);
        });

        // Entry的类型与列定义的不同
        Assert.assertThrows(ColumnTypeDiscrepancyException.class, () -> {
            Row errorRow3 = new Row();
            ArrayList<Entry> errorEntries = new ArrayList<Entry>();
            errorEntries.add(new Entry(4));
            errorEntries.add(new Entry(1111));
            errorEntries.add(new Entry("software"));
            errorRow3.appendEntries(errorEntries);
            this.table.insert(errorRow3);
        });

        // 违反约束
        Assert.assertThrows(ColumnRuleDiscrepancyException.class, () -> {
            Row errorRow4 = new Row();
            ArrayList<Entry> errorEntries = new ArrayList<Entry>();
            errorEntries.add(new Entry(5));
            errorEntries.add(new Entry("WXS"));
            errorEntries.add(new Entry(null));
            errorRow4.appendEntries(errorEntries);
            this.table.insert(errorRow4);
        });
    }

    @Test
    public void testDelete() {
        System.out.println("Delete Testing ...");

        Row testRow1 = new Row();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(1));
        entries.add(new Entry("CYN"));
        entries.add(new Entry("history"));
        testRow1.appendEntries(entries);
        this.table.insert(testRow1);

        Row testRow2 = new Row();
        entries = new ArrayList<Entry>();
        entries.add(new Entry(2));
        entries.add(new Entry("LSY"));
        entries.add(new Entry("software"));
        testRow2.appendEntries(entries);
        this.table.insert(testRow2);

        Row testRow3 = new Row();
        // 验证该行确实被删除

        this.table.delete(testRow1);
        Assert.assertThrows(KeyNotExistException.class, () -> {
            this.table.findRowByPrimaryKey(new Entry(1));
        });

        // 不存在的行删除报错
        Assert.assertThrows(KeyNotExistException.class, () -> {
            this.table.delete(testRow3);
        });

        // 验证该行确实被删除
        this.table.delete(testRow1);
        Assert.assertThrows(KeyNotExistException.class, () -> {
            this.table.findRowByPrimaryKey(new Entry(2));
        });
    }

    @Test
    public void testUpdate() {
        System.out.println("Update Testing ...");

        Row newRow = new Row();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(1));
        entries.add(new Entry("CYN"));
        entries.add(new Entry("history"));
        newRow.appendEntries(entries);
        this.table.insert(newRow);

        newRow = new Row();
        entries = new ArrayList<Entry>();
        entries.add(new Entry(2));
        entries.add(new Entry("LSY"));
        entries.add(new Entry("software"));
        newRow.appendEntries(entries);
        this.table.insert(newRow);

        Row updateRow = new Row();
        entries = new ArrayList<Entry>();
        entries.add(new Entry(1));
        entries.add(new Entry("KKBB"));
        entries.add(new Entry("history"));
        updateRow.appendEntries(entries);
        this.table.update(updateRow);
        Assert.assertEquals(updateRow.toString(),
                this.table.findRowByPrimaryKey(new Entry(1)).toString());

        updateRow = new Row();
        entries = new ArrayList<Entry>();
        entries.add(new Entry(3));
        entries.add(new Entry("Kiki"));
        entries.add(new Entry("history"));
        updateRow.appendEntries(entries);
        this.table.update(updateRow);
        Assert.assertEquals(updateRow.toString(),
                this.table.findRowByPrimaryKey(new Entry(3)).toString());

        // 想要修改的行不存在
        Assert.assertThrows(KeyNotExistException.class, () -> {
            Row errorRow = new Row();
            ArrayList<Entry> errorEntries = new ArrayList<Entry>();
            errorEntries.add(new Entry(4));
            errorEntries.add(new Entry("Lalala"));
            errorEntries.add(new Entry("history"));
            errorRow.appendEntries(errorEntries);
            this.table.update(errorRow);
        });

        // 验证原来的行是否被意外删除.
        Row originRow = new Row();
        ArrayList<Entry> originEntries = new ArrayList<Entry>();
        originEntries.add(new Entry(3));
        originEntries.add(new Entry("Mumumu"));
        originEntries.add(new Entry("history"));
        originRow.appendEntries(originEntries);
        Assert.assertEquals(originRow.toString(), this.table.findRowByPrimaryKey(new Entry(3)).toString());
    }

}
