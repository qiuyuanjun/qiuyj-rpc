package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.client.AsyncContext;
import com.qiuyj.qrpc.client.ResponseManager;
import com.qiuyj.qrpc.commons.async.DefaultFuture;
import com.qiuyj.qrpc.codec.protocol.MessageType;
import com.qiuyj.qrpc.codec.protocol.ResponseInfo;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器端异步服务调用返回客户端处理器
 * @author qiuyj
 * @since 2018-09-19
 */
public class AsyncResponseHandler extends ChannelInboundHandlerAdapter {

  /**
   * 接收服务器端返回的Future的临时容器
   * 该容器空闲的时候，容量应该为0
   */
  private final Map<String, DefaultFuture<Object>> tempFutureMap = new HashMap<>();

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    RpcMessage rpcMessage = (RpcMessage) msg;
    if (rpcMessage.getMessageType() == MessageType.ASYNC_RESPONSE_IMMEDIATELY) {
      DefaultFuture<Object> asyncFuture = new DefaultFuture<>();
      ResponseInfo responseInfo = (ResponseInfo) rpcMessage.getContent();
      // 将asyncFuture放置到tempFutureMap中
      tempFutureMap.put(responseInfo.getRequestId(), asyncFuture);
      AsyncContext.setFuture(responseInfo.getRequestId(), asyncFuture);
      // 唤醒等待线程
      ResponseManager.INSTANCE.done((ResponseInfo) rpcMessage.getContent());
    }
    else if (rpcMessage.getMessageType() == MessageType.ASYNC_RESPONSE) {
      ResponseInfo responseInfo = (ResponseInfo) rpcMessage.getContent();
      // 从AsyncContext得到对应的asyncFuture
      DefaultFuture<Object> asyncFuture = tempFutureMap.remove(responseInfo.getRequestId());
      asyncFuture.setSuccess(responseInfo.getResult());
    }
    else {
      ctx.fireChannelRead(msg);
    }
  }
}
