package com.qiuyj.qrpc.codec.protocol.heartbeat;

import com.qiuyj.qrpc.codec.protocol.MessageType;
import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.codec.protocol.ResponseInfo;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;

/**
 * @author qiuyj
 * @since 2018-09-08
 */
public class HeartbeatFactory {

  public static final String PING = "ping";

  public static final String PONG = "pong";

  /**
   * 得到心跳请求报文
   * @param requestId requestId
   */
  public static RpcMessage getRequestHeartbeat(String requestId) {
    HeartbeatRequest heartbeatRequestInfo = new HeartbeatRequest();
    heartbeatRequestInfo.setRequestId(requestId);
    heartbeatRequestInfo.setPing(PING);

    RpcMessage heartbeatMessage = new RpcMessage();
    heartbeatMessage.setMagic(RpcMessage.MAGIC_NUMBER);
    heartbeatMessage.setMessageType(MessageType.HEARTBEAT_REQUEST);
    heartbeatMessage.setContent(heartbeatRequestInfo);

    return heartbeatMessage;
  }

  /**
   * 得到心跳响应报文
   * @param requestId requestId
   */
  public static RpcMessage getResponseHeartbeat(String requestId) {
    ResponseInfo heartbeatResponse = new ResponseInfo();
    heartbeatResponse.setRequestId(requestId);
    heartbeatResponse.setResult(PONG);

    RpcMessage heartbeatMessage = new RpcMessage();
    heartbeatMessage.setMagic(RpcMessage.MAGIC_NUMBER);
    heartbeatMessage.setMessageType(MessageType.HEARTBEAT_RESPONSE);
    heartbeatMessage.setContent(heartbeatResponse);

    return heartbeatMessage;
  }

  public static class HeartbeatRequest extends RequestInfo {

    private static final long serialVersionUID = 6052979235574303488L;

    private String ping;

    public String getPing() {
      return ping;
    }

    public void setPing(String ping) {
      this.ping = ping;
    }
  }

}