package cn.edu.thssdb.service;

import cn.edu.thssdb.rpc.thrift.*;
import cn.edu.thssdb.utils.Global;
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
  public DisconnetResp disconnect(DisconnetResp req) throws TException {
    // TODO
    return null;
  }

  @Override
  public ExecuteStatementResp executeStatement(ExecuteStatementReq req) throws TException {
    // TODO
    return null;
  }
}
