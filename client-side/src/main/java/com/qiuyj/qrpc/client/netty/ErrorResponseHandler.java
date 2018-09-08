package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.client.ResponseManager;
import com.qiuyj.qrpc.client.RpcResponseErrorException;
import com.qiuyj.qrpc.codec.MessageType;
import com.qiuyj.qrpc.codec.ResponseInfo;
import com.qiuyj.qrpc.codec.RpcMessage;
import com.qiuyj.qrpc.commons.ErrorReason;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 处理服务器端错误返回的处理器
 * @author qiuyj
 * @since 2018-09-08
 */
public class ErrorResponseHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    RpcMessage rpcMessage = (RpcMessage) msg;
    if (rpcMessage.getMessageType() == MessageType.ERROR_RESPONSE) {
      ResponseInfo info = (ResponseInfo) rpcMessage.getContent();
      info.setResult(new RpcResponseErrorException((ErrorReason) info.getResult()));
      ResponseManager.INSTANCE.done(info);
    }
    else {
      // 不是错误类型，那么交给下一个处理器处理消息
      super.channelRead(ctx, msg);
    }
  }
}