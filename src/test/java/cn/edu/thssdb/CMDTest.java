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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CMDTest {

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
        visitor.auto_commit = false;
        executor.reset();
        visitor.bindQueryManager(executor, buffer);
        visitor.visit(tree);
        return parser;
    }

    public CMDTest() throws IOException {
        buffer = new LogBuffer();
        storage = new TransactionManager();
        executor = new QueryManager(storage, buffer);
    }

    public static void main(String[] args) throws IOException, ManagerNotReadyException {
        CMDTest test = new CMDTest();
        Scanner myObj = new Scanner(System.in);
        System.out.println("Running mode: " + System.getProperty("java.vm.name"));
        while (true) {
            String cmd = "";
            while (cmd.equals("")) {
                cmd = myObj.nextLine();
            }
            long start = System.currentTimeMillis();
            test.parse(cmd);
            long end = System.currentTimeMillis();
            String result = test.buffer.get();
            if (result.equals("")) {
                System.out.println(String.format("OK, in %d ms", end - start));
            }
            else {
                System.out.println(result);
                System.out.println("Current transaction aborted.");
            }
        }
    }
}
