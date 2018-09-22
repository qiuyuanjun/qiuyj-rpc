package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.client.ResponseManager;
import com.qiuyj.qrpc.client.RpcErrorResponseException;
import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.codec.protocol.MessageType;
import com.qiuyj.qrpc.codec.protocol.ResponseInfo;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author qiuyj
 * @since 2018-09-08
 */
@ChannelHandler.Sharable
public class ClientNonBusinessCommonHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    RpcMessage rpcMessage = (RpcMessage) msg;
    // 处理心跳返回
    if (rpcMessage.getMessageType() == MessageType.HEARTBEAT_RESPONSE) {
      ResponseInfo responseInfo = (ResponseInfo) rpcMessage.getContent();
      ResponseManager.INSTANCE.done(responseInfo);
    }
    // 处理错误返回
    else if (rpcMessage.getMessageType() == MessageType.ERROR_RESPONSE) {
      ResponseInfo info = (ResponseInfo) rpcMessage.getContent();
      info.setResult(new RpcErrorResponseException((ErrorReason) info.getResult()));
      ResponseManager.INSTANCE.done(info);
    }
    else {
      ctx.fireChannelRead(msg);
    }
  }
}