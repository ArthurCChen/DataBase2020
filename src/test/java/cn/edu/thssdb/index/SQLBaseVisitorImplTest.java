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
import sun.awt.windows.WBufferStrategy;

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
        assertEquals("SyntaxError: Primary key for more than one attribute not supported (for now).",
                buffer.get());
        // correct
        parse("create table test1(i int, j int not null, primary key (i))");
        assertEquals("createTable called, table name: test1, columns: " +
                        "[Column:{name: i, type: INT, primary: true, notNull: true, maxLength: 0}, " +
                        "Column:{name: j, type: INT, primary: false, notNull: true, maxLength: 0}]"
                , printer.getResult());
    }

//    @Test
}
