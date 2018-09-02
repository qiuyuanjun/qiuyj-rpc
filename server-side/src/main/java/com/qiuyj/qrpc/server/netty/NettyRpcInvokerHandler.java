package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.codec.MessageType;
import com.qiuyj.qrpc.codec.RequestInfo;
import com.qiuyj.qrpc.codec.ResponseInfo;
import com.qiuyj.qrpc.codec.RpcMessage;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.server.ServiceExporter;
import com.qiuyj.qrpc.server.messagehandler.MessageHandler;
import com.qiuyj.qrpc.server.messagehandler.RequestInfoMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiuyj
 * @since 2018-06-20
 */
class NettyRpcInvokerHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcInvokerHandler.class);

  private final ChannelGroup clients;

  private final MessageHandler<RequestInfo> serverMessageHandler;

  public NettyRpcInvokerHandler(ChannelGroup clients, ServiceExporter serviceExporter) {
    this.clients = clients;
    serverMessageHandler = new RequestInfoMessageHandler(serviceExporter);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // 表示一个客户端接入进来了，那么就存储这个客户端
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("A connection from client: " + ctx.channel().remoteAddress() + " has been linked.");
    }
    clients.add(ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("A connection from client: " + ctx.channel().remoteAddress() + " has been unlinked.");
    }
    // 如果一个客户端由于某种原因断开了连接，那么直接从group里面移除
    // 对于后续客户端重连，交给对应的客户端去做
    clients.remove(ctx.channel());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    // 通过解码器，可以将字节序列转成RpcMessage对象
    // 传到当前的ChannelHandler的时候，可以直接强转
    RequestInfo request = (RequestInfo) ((RpcMessage) msg).getContent();
    // 交给对应的messageHandler处理消息，并返回给客户端结果
    ResponseInfo response = serverMessageHandler.handle(request);
    response.setRequestId(request.getRequestId());
    // 将结果保存到RpcMessage里面
    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setMagic(RpcMessage.MAGIC_NUMBER);
    rpcMessage.setMessageType(MessageType.RPC_RESPONSE);
    rpcMessage.setContent(response);
    ctx.channel().writeAndFlush(rpcMessage);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (cause instanceof RpcException) {
      RpcException rpcException = (RpcException) cause;
      // 业务错误，发送错误信息给客户端
//      ctx.channel().writeAndFlush();
    }
    else {
      LOGGER.error("Error occured in channel: " + ctx.channel().id(), cause);
      ctx.channel().close();
      clients.remove(ctx.channel());
    }
  }
}