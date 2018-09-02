package com.qiuyj.qrpc.client.netty;

import com.qiuyj.api.AbstractConnection;
import com.qiuyj.qrpc.codec.RequestInfo;
import com.qiuyj.qrpc.codec.RpcMessage;
import com.qiuyj.qrpc.server.ResponseManager;
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
      ResponseManager.INSTANCE.waitForResponseResult(requestId);
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