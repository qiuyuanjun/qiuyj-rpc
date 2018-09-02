package com.qiuyj.qrpc.client.netty;

import com.qiuyj.api.AbstractConnection;
import com.qiuyj.qrpc.codec.RequestInfo;
import com.qiuyj.qrpc.codec.RpcMessage;
import com.qiuyj.qrpc.client.ResponseManager;
import io.netty.channel.socket.SocketChannel;

/**
 * @author qiuyj
 * @since 2018-08-30
 */
public class NettyConnection extends AbstractConnection {

  /** 与服务器之间的连接通道（netty） */
  private SocketChannel channel;

  public NettyConnection(SocketChannel channel) {
    this.channel = channel;
  }

  @Override
  protected Object doSend(Object message) {
    if (message instanceof RpcMessage) {
      String requestId = ((RequestInfo) ((RpcMessage) message).getContent()).getRequestId();
      channel.writeAndFlush(message);
      // 同步等待结果
      ResponseManager.INSTANCE.waitForResponseResult(requestId);
      // 服务器端已经返回结果，那么将结果设置到ResponseManager里面
      return ResponseManager.INSTANCE.getResult(requestId);
    }
    return null;
  }

  @Override
  public void close() {
    channel.close();
  }

  // ------ for internal useage ------
  NettyConnection() {

  }

  void setSocketChannel(SocketChannel socketChannel) {
    channel = socketChannel;
  }

  void initializeIdleStateChecker() {
    super.initialize();
  }
}