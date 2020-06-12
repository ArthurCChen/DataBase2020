package cn.edu.thssdb;

import cn.edu.thssdb.exception.ManagerNotReadyException;
import cn.edu.thssdb.memory_db.TransactionManager;
import cn.edu.thssdb.parser.SQLBaseVisitorImpl;
import cn.edu.thssdb.parser.SQLLexer;
import cn.edu.thssdb.parser.SQLParser;
import cn.edu.thssdb.query.QueryManager;
import cn.edu.thssdb.utils.LogBuffer;
import cn.edu.thssdb.utils.Physical2LogicalInterface;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Finally I can test the whole pipeline.
 * From parsing to execution, to multi user.
 */
public class PipelineTest {

    private LogBuffer buffer;
    Physical2LogicalInterface storage;
    private QueryManager executor;

    private void parse(String s) throws ManagerNotReadyException {
        long t1 = System.currentTimeMillis();
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(s));
        long t2 = System.currentTimeMillis();
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        long t3 = System.currentTimeMillis();
        SQLParser parser = new SQLParser(tokenStream);
        long t4 = System.currentTimeMillis();
        parser.removeErrorListeners();
        long t5 = System.currentTimeMillis();
        parser.addErrorListener(buffer);
        long t6 = System.currentTimeMillis();

        ParseTree tree = parser.parse();
        long t7 = System.currentTimeMillis();
        SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
        long t8 = System.currentTimeMillis();
        executor.reset();
        long t9 = System.currentTimeMillis();
        visitor.bindQueryManager(executor, buffer);
        long t10 = System.currentTimeMillis();
        visitor.visit(tree);
        long t11 = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(t2-t1).append(" "); //!
        sb.append(t3-t2).append(" ");
        sb.append(t4-t3).append(" "); //!
        sb.append(t5-t4).append(" ");
        sb.append(t6-t5).append(" ");
        sb.append(t7-t6).append(" "); // !!
        sb.append(t8-t7).append(" ");
        sb.append(t9-t8).append(" ");
        sb.append(t10-t9).append(" ");
        sb.append(t11-t10).append(" "); //!!
//        System.out.println(sb.toString());
    }

    @Before
    public void setUp() throws IOException {
        long t1 = System.currentTimeMillis();
        BufferedWriter log = new BufferedWriter(new FileWriter("./SQLBaseVisitorImplTest.log"));
        buffer = new LogBuffer(log);
        storage = new TransactionManager();
        executor = new QueryManager(storage, buffer);
        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);
    }

    @Test
    public void create_drop_table_test() throws ManagerNotReadyException {
        // create a table, show another table, should abort
        parse("create table test1(i int, j int not null, primary key (i)); show table table1;");
        assertEquals("SemanticError: can not show a non-exist table.",
                buffer.get());
        // create a table and show it
        parse("create table test1(i int, j int not null, primary key (i)); show table test1;");
        assertEquals("", buffer.get());
        // create the table again, should log error
        parse("create table test1(i int, j int not null, primary key (i))");
        assertEquals("SemanticError: the table already exists.", buffer.get());
        // drop a table that do not exist
        parse("drop table test2; show table test2;");
        assertEquals("SemanticError: can not drop a non-exist table.", buffer.get());
        parse("drop table test1");
        assertEquals("", buffer.get());
        parse("drop table test1");
        assertEquals("SemanticError: can not drop a non-exist table.", buffer.get());
    }

    @Test
    public void insert_delete_row_test() throws ManagerNotReadyException {
        // create a table, insert two rows, and show it
        parse("create table test1(i int, j int not null, primary key (i));" +
                "insert into test1 values (1, 2);" +
                "insert into test1 (i, j) values (3,4);" +
                "insert into test1 (i, j) values (2,4);" +
                "insert into test1 values (5,5);" +
                "show table test1;");
        assertEquals("", buffer.get());

        // insert by wrong type
        parse("insert into test1 values ('ha', 2);");
        assertEquals("SemanticError: convert fail.", buffer.get());

        // insert duplicate row
        parse("insert into test1 values (1, 2);");
        assertEquals("SemanticError: fail to insert row 1, 2 into table test1.", buffer.get());

        // delete with wrong condition
        parse("delete from test1 where i = k;");
        assertEquals("SemanticError: table does not have column: k.", buffer.get());

        // delete with constant with different type
        parse("delete from test1 where 2.5 = 'ha';");
        assertEquals("SemanticError: 2.5 and 'ha' have different types.", buffer.get());

        // delete row (1,2)
        parse("delete from test1 where i=1 && j=2 && i<>2;");
        assertEquals("", buffer.get());

        // delete row (5,5)
        parse("delete from test1 where i=j;");
        assertEquals("", buffer.get());

        // delete row (?,4)
        parse("delete from test1 where j=4;show table test1;");
        assertEquals("", buffer.get());

    }

    @Test
    public void select_test() throws ManagerNotReadyException {
        long t1 = System.currentTimeMillis();
        // create a table, insert two rows, and show it
        parse("create table test1(i int, j int not null, primary key (i));" +
                "insert into test1 values (1, 2);" +
                "insert into test1 (i, j) values (3,4);" +
                "insert into test1 (i, j) values (2,4);" +
                "insert into test1 values (5,5);" +
                "show table test1;");
        assertEquals("", buffer.get());
        long t2 = System.currentTimeMillis();

        parse("create table test2(i int, j int not null, primary key (i));" +
                "insert into test2 values (1, 2);" +
                "insert into test2 (i, j) values (3,4);" +
                "insert into test2 (i, j) values (2,4);" +
                "insert into test2 values (5,5);" +
                "show table test2;");
        assertEquals("", buffer.get());
        long t3 = System.currentTimeMillis();

        parse("select test1.i, test2.j from test1 join test2 on test1.i = test2.i;");
        assertEquals("", buffer.get());

        parse("update test1 set f = 100 where j = 4; show table test1;");
        assertEquals("SemanticError: column f not found.", buffer.get());

        parse("update test1 set j = 100 where j = 4; show table test1;");
        assertEquals("", buffer.get());
        long t4 = System.currentTimeMillis();
//        System.out.println(String.format("%d %d %d", t2-t1, 0, 0));
    }
}
