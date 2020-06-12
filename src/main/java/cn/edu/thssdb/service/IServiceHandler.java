package cn.edu.thssdb.service;

import cn.edu.thssdb.exception.ManagerNotReadyException;
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
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) throws TException {
    // TODO

//    LogBuffer buffer = null;
////    Physical2LogicalInterface storage;
//    QueryManager executor = null;
//
//    SQLLexer lexer = new SQLLexer(CharStreams.fromString(req.statement));
//    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
//    SQLParser parser = new SQLParser(tokenStream);
//    parser.removeErrorListeners();
//    parser.addErrorListener(buffer);
//    ParseTree tree = parser.parse();
//    SQLBaseVisitorImpl visitor = new SQLBaseVisitorImpl();
////    visitor.auto_commit = false;
//    executor.reset();
////    visitor.bindQueryManager(executor, buffer);
//    visitor.visit(tree);

    ExecuteStatementResp resp = new ExecuteStatementResp();
    resp.setIsAbort(false);
    resp.setHasResult(false);
    resp.setStatus(new Status(Global.SUCCESS_CODE).setMsg("success"));
    return resp;
  }
}
