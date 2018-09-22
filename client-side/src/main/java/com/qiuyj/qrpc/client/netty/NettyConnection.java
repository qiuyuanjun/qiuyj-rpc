package com.qiuyj.qrpc.client.netty;

import com.qiuyj.api.AbstractConnection;
import com.qiuyj.api.HealthState;
import com.qiuyj.api.client.Client;
import com.qiuyj.qrpc.client.requestid.HeartbeatRequestId;
import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;
import com.qiuyj.qrpc.client.ResponseManager;
import com.qiuyj.qrpc.codec.protocol.heartbeat.HeartbeatFactory;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * @author qiuyj
 * @since 2018-08-30
 */
public class NettyConnection extends AbstractConnection {

  private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NettyConnection.class);

  /** 与服务器之间的连接通道（netty） */
  private SocketChannel channel;

  public NettyConnection(Client client, SocketChannel channel) {
    super(client);
    this.channel = channel;
  }

  @Override
  protected HealthState healthCheck() {
    String requestId = HeartbeatRequestId.INSTANCE.nextRequestId();
    // 向服务器端发送心跳包
    channel.writeAndFlush(HeartbeatFactory.getRequestHeartbeat(requestId));
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Sending heartbeat packet...");
      }
      ResponseManager.INSTANCE.waitForResponseResult(requestId);
    }
    catch (TimeoutException e) {
      // 表明当前connection已经和远程服务器断开连接
      // 那么就需要重新连接远程服务器
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Heartbeat response timeout. Reconnect remote server.");
      }
      return HealthState.UNHEALTH;
    }
    // 判断服务器端心跳返回的对象是否是pong
    String pong = ResponseManager.INSTANCE.getResult(requestId).getResult().toString();
    // 返回的不是pong，那么也表示不是健康状态
    return HeartbeatFactory.PONG.equals(pong) ? HealthState.HEALTH : HealthState.UNHEALTH;
  }

  @Override
  protected Object doSend(Object message) {
    if (message instanceof RpcMessage) {
      String requestId = ((RequestInfo) ((RpcMessage) message).getContent()).getRequestId();
      channel.writeAndFlush(message);
      // 同步等待结果
      try {
        ResponseManager.INSTANCE.waitForResponseResult(requestId);
      }
      catch (TimeoutException e) {
        throw new IllegalStateException(e);
      }
      // 服务器端已经返回结果，那么将结果设置到ResponseManager里面
      return ResponseManager.INSTANCE.getResult(requestId);
    }
    else {
      throw new IllegalStateException("Unexpect request data type.");
    }
  }

  @Override
  public void close() {
    super.close();
    channel.close();
  }

  // ------ for internal useage ------
  NettyConnection(Client client) {
    super(client);
  }

  void setSocketChannel(SocketChannel socketChannel) {
    channel = socketChannel;
  }

  void initializeIdleStateChecker() {
    super.initialize();
  }
}