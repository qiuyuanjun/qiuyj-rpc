package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.codec.MessageType;
import com.qiuyj.qrpc.codec.ResponseInfo;
import com.qiuyj.qrpc.codec.RpcMessage;
import com.qiuyj.qrpc.client.ResponseManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author qiuyj
 * @since 2018-09-02
 */
public class NettyRpcClientMessageHandler extends ChannelInboundHandlerAdapter {

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
}
