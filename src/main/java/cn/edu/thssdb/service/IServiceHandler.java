package cn.edu.thssdb.service;

import cn.edu.thssdb.exception.ManagerNotReadyException;
import cn.edu.thssdb.memory_db.TransactionManager;
import cn.edu.thssdb.parser.SQLBaseVisitorImpl;
import cn.edu.thssdb.parser.SQLLexer;
import cn.edu.thssdb.parser.SQLParser;
import cn.edu.thssdb.query.QueryManager;
import cn.edu.thssdb.rpc.thrift.*;
import cn.edu.thssdb.server.ThssDB;
import cn.edu.thssdb.utils.Global;
import cn.edu.thssdb.utils.LogBuffer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;

public class IServiceHandler implements IService.Iface {

  private LogBuffer buffer = null;
  private TransactionManager storage = null;
  private QueryManager executor = null;
  private long timeout = 30000; // 30s
  private ExecuteStatementResp resp = null;

  private void execute(String s) throws ManagerNotReadyException {
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
    visitor.bind_resp(this.resp);
    visitor.visit(tree);
  }

  public IServiceHandler() {
    buffer = new LogBuffer();
    storage = new TransactionManager();
    executor = new QueryManager(storage, buffer);
  }

  @Override
  public GetTimeResp getTime(GetTimeReq req) throws TException {
    GetTimeResp resp = new GetTimeResp();
    resp.setTime(new Date().toString());
    resp.setStatus(new Status(Global.SUCCESS_CODE));
    return resp;
  }

  @Override
  public ConnectResp connect(ConnectReq req) throws TException {
    // TODO
    ConnectResp resp = new ConnectResp();
    resp.setSessionId(0);
    resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));

    //手动执行一次start transaction
    SQLLexer lexer = new SQLLexer(CharStreams.fromString(Global.START_TRANSACTION));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQLParser parser = new SQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(buffer);
    ParseTree tree = parser.parse();
    SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
    visitor.auto_commit = false;
    executor.reset();
    visitor.bindQueryManager(executor, buffer);
    visitor.bind_resp(this.resp);
    visitor.visit(tree);

    return resp;
  }

  @Override
  public DisconnectResp disconnect(DisconnectReq req) throws TException {
    //手动执行一次commit
    SQLLexer lexer = new SQLLexer(CharStreams.fromString(Global.COMMIT));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQLParser parser = new SQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(buffer);
    ParseTree tree = parser.parse();
    SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
    visitor.auto_commit = false;
    executor.reset();
    visitor.bindQueryManager(executor, buffer);
    visitor.bind_resp(this.resp);
    visitor.visit(tree);

    DisconnectResp resp = new DisconnectResp();
    resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));

    return resp;
  }

  @Override
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) {

    //sessionId验证机制 + 权限
//    if (!ThssDB.getUserState(req.sessionId)) {
//      manager = ThssDB.getUserManagerCopy(req.sessionId);
//    } else {
//      manager = ThssDB.getManager();
//    }

    ExecuteStatementResp resp = new ExecuteStatementResp();
    this.resp = resp;

    // subject to change when executing
    resp.setIsAbort(false);
    resp.setHasResult(false);

    SQLLexer lexer = new SQLLexer(CharStreams.fromString(req.statement));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQLParser parser = new SQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(buffer);
    ParseTree tree = parser.parse();
    SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
    visitor.auto_commit = false;
    executor.reset();
    visitor.bindQueryManager(executor, buffer);
    visitor.bind_resp(this.resp);
    visitor.visit(tree);
//    if (!resp.isSetStatus()) {
//      try {
//        resp.wait(timeout);
//      } catch (InterruptedException e) {
//        resp.setStatus(new Status(Global.FAILURE_CODE).setMsg(
//                "InternalError: user process interrupted."));
//      }
//    }
    String error_code = buffer.get();
    if (!error_code.equals("")) {
      resp.setStatus(new Status(Global.FAILURE_CODE).setMsg(error_code));
    }
    else {
      resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("ok"));
    }
    return resp;

//    resp.setIsAbort(false);
//    resp.setHasResult(false);
//    resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));
  }

  //not used
  @Override
  public TransactionResp transaction(TransactionReq req) throws TException {
    String cmd = req.getStatement();
    TransactionResp resp = new TransactionResp();

    // subject to change when executing
    resp.setIsAbort(false);

    SQLLexer lexer = new SQLLexer(CharStreams.fromString(req.statement));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQLParser parser = new SQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(buffer);
    ParseTree tree = parser.parse();
    SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
    visitor.auto_commit = false;
    executor.reset();
    visitor.bindQueryManager(executor, buffer);
    visitor.bind_resp(this.resp);
    visitor.visit(tree);

    String error_code = buffer.get();
    if (!error_code.equals("")) {
      resp.setStatus(new Status(Global.FAILURE_CODE).setMsg(error_code));
    }
    else {
      resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("ok"));
    }
    return resp;
  }
}


