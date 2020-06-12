package cn.edu.thssdb.service;

import cn.edu.thssdb.exception.ManagerNotReadyException;
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
    return null;
  }

  @Override
  public DisconnectResp disconnect(DisconnectReq req) throws TException {
    return null;
  }

  @Override
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) throws TException, ManagerNotReadyException {

    ExecuteStatementResp resp = new ExecuteStatementResp();
    this.resp = resp;

    // subject to change when executing
    resp.setIsAbort(false);
    resp.setHasResult(false);

    execute(req.statement);
    if (!resp.isSetStatus()) {
      try {
        resp.wait(timeout);
      } catch (InterruptedException e) {
        resp.setStatus(new Status(Global.FAILURE_CODE).setMsg(
                "InternalError: user process interrupted."));
      }
    }
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
}
