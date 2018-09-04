package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.client.ResponseManager;
import com.qiuyj.qrpc.codec.MessageType;
import com.qiuyj.qrpc.codec.ResponseInfo;
import com.qiuyj.qrpc.codec.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;

/**
 * @author qiuyj
 * @since 2018-09-02
 */
public class NettyRpcClientMessageHandler extends ChannelInboundHandlerAdapter {

  private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NettyRpcClientMessageHandler.class);

  private final NettyConnection connection;

  public NettyRpcClientMessageHandler(NettyConnection nettyConnection) {
    connection = nettyConnection;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    connection.initializeIdleStateChecker();
    super.channelActive(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    RpcMessage rpcMessage = (RpcMessage) msg;
    if (rpcMessage.getMessageType() == MessageType.RPC_RESPONSE) {
      ResponseInfo response = (ResponseInfo) rpcMessage.getContent();
      // 设置结果
      ResponseManager.INSTANCE.done(response);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // 记录异常信息
    LOGGER.error(cause);
    // 检测死锁
    // 关闭连接
    if (cause instanceof IOException) {
      connection.close();
    }
  }
}
