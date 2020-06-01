package cn.edu.thssdb;

import cn.edu.thssdb.exception.ManagerNotReadyException;
import cn.edu.thssdb.memory_db.MDBManager;
import cn.edu.thssdb.parser.ParserPrinter;
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
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * Finally I can test the whole pipeline.
 * From parsing to execution, to multi user.
 */
public class PipelineTest {

    private LogBuffer buffer;
    Physical2LogicalInterface storage;
    private QueryManager executor;

    private SQLParser parse(String s) throws ManagerNotReadyException {
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(s));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(buffer);

        ParseTree tree = parser.parse();
        SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
        executor.reset();
        visitor.bindQueryManager(executor, buffer);
        visitor.visit(tree);
        return parser;
    }

    @Before
    public void setUp() throws IOException {
        BufferedWriter log = new BufferedWriter(new FileWriter("./SQLBaseVisitorImplTest.log"));
        buffer = new LogBuffer(log);
        storage = new MDBManager();
        executor = new QueryManager(storage, buffer);
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
}
