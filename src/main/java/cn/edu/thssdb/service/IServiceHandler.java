package cn.edu.thssdb.service;
import cn.edu.thssdb.memory_db.TransactionManager;
import cn.edu.thssdb.parser.SQLBaseVisitorImpl;
import cn.edu.thssdb.parser.SQLLexer;
import cn.edu.thssdb.parser.SQLParser;
import cn.edu.thssdb.query.QueryManager;
import cn.edu.thssdb.rpc.thrift.*;
import cn.edu.thssdb.utils.Global;
import cn.edu.thssdb.utils.LogBuffer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class IServiceHandler implements IService.Iface {

  private LogBuffer buffer;
  private TransactionManager storage;
  private HashMap<Long, QueryManager> executor;
  private ExecuteStatementResp resp = null;
  private long max_session = -1;

  private void execute(String s, long session_id){
    SQLLexer lexer = new SQLLexer(CharStreams.fromString(s));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SQLParser parser = new SQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(buffer);
    ParseTree tree = parser.parse();
    SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
    visitor.auto_commit = false;
    QueryManager session = executor.get(session_id);
    session.reset();
    visitor.bindQueryManager(session, buffer);
    visitor.bind_resp(this.resp);
    visitor.visit(tree);
  }

  // return session id
  private long add_session() {
    max_session += 1;
    QueryManager manager = new QueryManager(storage, buffer);
    executor.put(max_session, manager);
    return max_session;
  }

  private boolean remove_session(long session_id) {
    if (executor.containsKey(session_id)) {
      executor.remove(session_id);
      return true;
    }
    return false;
  }

  public IServiceHandler() {
    buffer = new LogBuffer();
    storage = new TransactionManager();
    executor = new HashMap<>();
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
    long id = add_session();
    ConnectResp resp = new ConnectResp();
    resp.setSessionId(id);
    resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));
    execute(Global.START_TRANSACTION, id);
    return resp;
  }

  @Override
  public DisconnectResp disconnect(DisconnectReq req) throws TException {
    boolean success = remove_session(req.sessionId);
    DisconnectResp resp = new DisconnectResp();
    if (success) {
      execute(Global.COMMIT, req.sessionId);
      resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));
    }
    else {
      resp.setStatus(new Status(Global.FAILURE_CODE).setMsg("No such session id."));
    }
    return resp;
  }

  @Override
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) {

    ExecuteStatementResp resp = new ExecuteStatementResp();
    this.resp = resp;

    // subject to change when executing
    resp.setIsAbort(false);
    resp.setHasResult(false);
    if (req.statement.contains("create database") || req.statement.contains("use test")) {
      resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("ok"));
      return resp;
    }
    execute(req.statement, req.sessionId);

    QueryManager manager = executor.get(req.sessionId);
    while (!manager.task_clear()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {
        ;
      }
    }

    String error_code = buffer.get();
    if (!error_code.equals("")) {
      resp.setStatus(new Status(Global.FAILURE_CODE).setMsg(error_code));
      System.out.println(error_code);
    }
    else {
      resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("ok"));
    }
    return resp;
  }

  //not used
  @Override
  public TransactionResp transaction(TransactionReq req) throws TException {
    TransactionResp resp = new TransactionResp();
    resp.setIsAbort(false);
    return resp;
  }
}