package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.commons.protocol.MessageType;
import com.qiuyj.qrpc.commons.protocol.RpcMessage;
import com.qiuyj.qrpc.commons.protocol.heartbeat.HeartbeatFactory;
import com.qiuyj.qrpc.server.CloseChannelException;
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

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    RpcMessage rpcMessage = (RpcMessage) msg;
    if (rpcMessage.getMessageType() == MessageType.HEARTBEAT_REQUEST) {
      HeartbeatFactory.HeartbeatRequest heartbeatRequest = (HeartbeatFactory.HeartbeatRequest) rpcMessage.getContent();
      if (HeartbeatFactory.PING.equals(heartbeatRequest.getPing())) {
        ctx.channel().writeAndFlush(HeartbeatFactory.getResponseHeartbeat(heartbeatRequest.getRequestId()));
      }
      else {
        throw new CloseChannelException("Error heartbeat request data.");
      }
    }
    else {
      ctx.fireChannelRead(msg);
    }
  }
}