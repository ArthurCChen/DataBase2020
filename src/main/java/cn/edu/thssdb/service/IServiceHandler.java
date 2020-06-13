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
import java.util.Date;

public class IServiceHandler implements IService.Iface {

  private LogBuffer buffer = null;
  private TransactionManager storage = null;
  private QueryManager executor = null;
  private long timeout = 30000; // 30s
  private ExecuteStatementResp resp = null;

  private void execute(String s){
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
    execute(Global.START_TRANSACTION);
    String error_code = buffer.get();
    return resp;
  }

  @Override
  public DisconnectResp disconnect(DisconnectReq req) throws TException {
    //手动执行一次commit
    execute(Global.COMMIT);
    DisconnectResp resp = new DisconnectResp();
    resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));
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
    execute(req.statement);
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