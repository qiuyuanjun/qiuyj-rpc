package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.codec.MessageType;
import com.qiuyj.qrpc.codec.RpcMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 服务器端心跳处理器
 * @author qiuyj
 * @since 2018-09-05
 */
@ChannelHandler.Sharable
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {

  /**
   * 心跳包
   */
   private static final RpcMessage HEARTBEAT;

   static {
     HEARTBEAT = new RpcMessage();
     HEARTBEAT.setMagic(RpcMessage.MAGIC_NUMBER);
     HEARTBEAT.setMessageType(MessageType.HEARTBEAT_RESPONSE);
   }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    RpcMessage rpcMessage = (RpcMessage) msg;
    if (rpcMessage.getMessageType() == MessageType.HEARTBEAT_REQUEST) {
      // 这里是否需要增加一个requestId？
      ctx.channel().writeAndFlush(HEARTBEAT);
    }
    else {
      ctx.fireChannelRead(msg);
    }
  }
}