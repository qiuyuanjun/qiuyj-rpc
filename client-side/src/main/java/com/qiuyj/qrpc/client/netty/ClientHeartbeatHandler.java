package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.client.ResponseManager;
import com.qiuyj.qrpc.commons.protocol.MessageType;
import com.qiuyj.qrpc.commons.protocol.ResponseInfo;
import com.qiuyj.qrpc.commons.protocol.RpcMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author qiuyj
 * @since 2018-09-08
 */
@ChannelHandler.Sharable
public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    RpcMessage rpcMessage = (RpcMessage) msg;
    if (rpcMessage.getMessageType() == MessageType.HEARTBEAT_RESPONSE) {
      ResponseInfo responseInfo = (ResponseInfo) rpcMessage.getContent();
      ResponseManager.INSTANCE.done(responseInfo);
    }
    else {
      super.channelRead(ctx, msg);
    }
  }
}