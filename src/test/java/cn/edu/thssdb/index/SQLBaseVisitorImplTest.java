package cn.edu.thssdb.index;

import cn.edu.thssdb.exception.ManagerNotReadyException;
import cn.edu.thssdb.parser.SQLBaseVisitorImpl;
import cn.edu.thssdb.parser.SQLLexer;
import cn.edu.thssdb.parser.SQLParser;
import cn.edu.thssdb.utils.LogBuffer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SQLBaseVisitorImplTest {

    private LogBuffer buffer;
    private ParserPrinter printer = new ParserPrinter();

    private SQLParser parse(String s) throws ManagerNotReadyException {
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(s));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(buffer);

        ParseTree tree = parser.parse();
        SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
        visitor.bindQueryManager(printer, buffer);
        visitor.visit(tree);
        return parser;
    }

    @Before
    public void setUp() throws IOException {
        BufferedWriter log = new BufferedWriter(new FileWriter("./SQLBaseVisitorImplTest.log"));
        buffer = new LogBuffer(log);
    }

    @Test
    public void create_table() throws ManagerNotReadyException {
        // no primary key
        parse("create table test1(i int)");
        assertEquals("SyntaxError: you need to specify a primary key.",
                buffer.get());
        // wrong primary key
        parse("create table test1(i int, primary key (j))");
        assertEquals("SyntaxError: specified primary key not found.",
                buffer.get());
        // multiple primary key
        parse("create table test1(i int, j int, primary key (i, j))");
        assertEquals("NotImplementError: Primary key for more than one attribute not supported (for now).",
                buffer.get());
        // correct
        parse("create table test1(i int, j int not null, primary key (i))");
        assertEquals("createTable called, table name: test1, columns: " +
                        "[Column:{name: i, type: INT, primary: true, notNull: true, maxLength: 0}, " +
                        "Column:{name: j, type: INT, primary: false, notNull: true, maxLength: 0}]"
                , printer.getResult());
    }

    @Test
    public void delete() throws ManagerNotReadyException {
        // without condition
        parse("delete from table1");
        assertEquals("deleteRows called, table name: table1, predicate: null",
                printer.getResult());
        // with condition
        parse("delete from table1 where a = b");
        assertEquals("deleteRows called, table name: table1, predicate: (Column operand: a==Column operand: b)",
                printer.getResult());
    }

    @Test
    public void drop_table() throws ManagerNotReadyException {
        parse("drop table if exists table1");
        assertEquals("NotImplementError: if exists not implemented.", buffer.get());
        parse("drop table table1");
        assertEquals("dropTable called, table name: table1", printer.getResult());
    }

    @Test
    public void quit() throws ManagerNotReadyException {
        parse("quit");
        assertEquals("quit called.", printer.getResult());
    }

    @Test
    public void show_table() throws ManagerNotReadyException {
        parse("show table table1");
        assertEquals("showTable called, table name: table1", printer.getResult());
    }

    @Test
    public void insert() throws  ManagerNotReadyException {
        // wrong number of values
        parse("insert into table1(a, b) values (1)");
        assertEquals("SyntaxError: rows should have the same size as schema size.",
                buffer.get());
        // correct number of values
        parse("insert into table1(a, b) values (1, 2)");
        assertEquals("insertEntry called, table name: table1, columns: " +
                        "[Column:{name: a, type: null, primary: false, notNull: false, maxLength: 0}, " +
                        "Column:{name: b, type: null, primary: false, notNull: false, maxLength: 0}], " +
                        "entries: [1, 2]",
                printer.getResult());
        // wrong field
        parse("insert into table1 values (1,2,3,ha)");
        assertEquals("Syntax Error: 1:33 mismatched input 'ha' expecting {K_NULL, NUMERIC_LITERAL, STRING_LITERAL}",
                buffer.get());
        // without field specification
        parse("insert into table1 values (1,2,3,'ha')");
        assertEquals("insertEntry called, table name: table1, " +
                        "columns: null, " +
                        "entries: [1, 2, 3, 'ha']",
                printer.getResult());
    }

    @Test
    public void select() throws ManagerNotReadyException {
        // correct
        parse("select a from table1");
        assertEquals("select called, result columns: " +
                "[Column:{name: a, type: null, primary: false, notNull: false, maxLength: 0}]," +
                " virtual table: VirtualTable: {tables: [table1], condition: null}, " +
                "conditions: null",
                printer.getResult());
        // distinct
        parse("select distinct a from table1");
        assertEquals("NotImplementError: 'distinct' not implemented.", buffer.get());
        // all
        parse("select all a from table1");
        assertEquals("NotImplementError: 'all' not implemented.", buffer.get());
        // multiple columns
        parse("select a, b from table1");
        assertEquals("select called, result columns: " +
                "[Column:{name: a, type: null, primary: false, notNull: false, maxLength: 0}, " +
                "Column:{name: b, type: null, primary: false, notNull: false, maxLength: 0}], " +
                "virtual table: VirtualTable: {tables: [table1], condition: null}, " +
                "conditions: null", printer.getResult());
        // multiple query tables
        parse("select a from table1, table2");
        assertEquals("NotImplementError: query from more than one table not implemented.",
                buffer.get());
    }

    @Test
    public void update() throws ManagerNotReadyException {
        parse("update table1 set a = 2");
        assertEquals("update called, table name: table1, value: a, condition: null", printer.getResult());
    }

    @Test
    public void condition() throws ManagerNotReadyException {
        // logical
        parse("update table1 set a = 2 where a = b && b = c && c = d || d = e");
        assertEquals("update called, table name: table1, value: a, " +
                "condition: ((((Column operand: a==Column operand: b) " +
                "and (Column operand: b==Column operand: c)) " +
                "and (Column operand: c==Column operand: d)) " +
                "or (Column operand: d==Column operand: e))", printer.getResult());
        // compare
        parse("update table1 set a = 2 where b <> c");
        assertEquals("update called, table name: table1, value: a, " +
                "condition: (Column operand: b!=Column operand: c)", printer.getResult());
        parse("update table1 set a = 2 where b > c");
        assertEquals("update called, table name: table1, value: a, " +
                "condition: (Column operand: b>Column operand: c)", printer.getResult());
        parse("update table1 set a = 2 where b < c");
        assertEquals("update called, table name: table1, value: a, " +
                "condition: (Column operand: b<Column operand: c)", printer.getResult());
        parse("update table1 set a = 2 where b >= c");
        assertEquals("update called, table name: table1, value: a, " +
                "condition: (Column operand: b>=Column operand: c)", printer.getResult());
        parse("update table1 set a = 2 where b <= c");
        assertEquals("update called, table name: table1, value: a, " +
                "condition: (Column operand: b<=Column operand: c)", printer.getResult());
    }
}
