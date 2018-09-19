package com.qiuyj.qrpc.server.messagehandler.netty;

import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.commons.async.DefaultFuture;
import com.qiuyj.qrpc.commons.protocol.MessageType;
import com.qiuyj.qrpc.commons.protocol.RequestInfo;
import com.qiuyj.qrpc.commons.protocol.ResponseInfo;
import com.qiuyj.qrpc.commons.protocol.RpcMessage;
import com.qiuyj.qrpc.server.netty.NettyChannelAttachedRequestInfo;
import com.qiuyj.qrpc.server.ServiceExporter;
import com.qiuyj.qrpc.server.messagehandler.RequestInfoMessageHandler;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * @author qiuyj
 * @since 2018-09-18
 */
public class NettyChannelAwareRequestInfoMessageHandler extends RequestInfoMessageHandler {

  private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NettyChannelAwareRequestInfoMessageHandler.class);

  public NettyChannelAwareRequestInfoMessageHandler(ExecutorService asyncExecutor, ServiceExporter serviceExporter) {
    super(asyncExecutor, serviceExporter);
  }

  @Override
  protected void sendAsyncResponse(RequestInfo message, DefaultFuture future) {
    // 得到服务调用的结果
    Object result;
    try {
      result = future.get();
    } catch (InterruptedException e) {
      LOGGER.error("非正常业务的异常.", e);
      throw new RpcException(message.getRequestId(), ErrorReason.ABNORMAL_BUSINESS_ERROR);
    } catch (ExecutionException e) {
      LOGGER.error("服务异步执行抛出异常.", e);
      throw new RpcException(message.getRequestId(), ErrorReason.ASYNC_EXECUTE_SERVICE_ERROR);
    }
    NettyChannelAttachedRequestInfo requestInfo = (NettyChannelAttachedRequestInfo) message;
    Channel ch = requestInfo.getChannel();
    // 将结果封装成ResponseInfo
    ResponseInfo responseInfo = new ResponseInfo();
    responseInfo.setResult(result);
    responseInfo.setRequestId(requestInfo.getRequestId());

    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setMagic(RpcMessage.MAGIC_NUMBER);
    rpcMessage.setMessageType(MessageType.ASYNC_RESPONSE);
    rpcMessage.setContent(responseInfo);

    ch.writeAndFlush(rpcMessage);
  }
}