package com.qiuyj.qrpc.client.proxy.jdk;

import com.qiuyj.api.Connection;
import com.qiuyj.qrpc.codec.MessageType;
import com.qiuyj.qrpc.codec.RequestInfo;
import com.qiuyj.qrpc.codec.ResponseInfo;
import com.qiuyj.qrpc.codec.RpcMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qiuyj
 * @since 2018-09-01
 */
public class ServiceInstanceJdkProxyHandler implements InvocationHandler {

  private Class<?> serviceInterface;

  private Connection connection;

  private AtomicLong longSeq = new AtomicLong(0);

  public ServiceInstanceJdkProxyHandler(Class<?> serviceInterface, Connection connection) {
    this.serviceInterface = serviceInterface;
    this.connection = connection;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    RpcMessage rpcMessage = getRequestRpcMessage(serviceInterface, method, args);
    Object result = connection.send(rpcMessage);
    if (result instanceof ResponseInfo) {
      ResponseInfo response = (ResponseInfo) result;
      if (response.hasError()) {
        throw response.getCause();
      }
      else {
        return response.getResult();
      }
    }
    else {
      return result;
    }
  }

  private RpcMessage getRequestRpcMessage(Class<?> serviceInterface, Method method, Object[] args) {
    RequestInfo rpcRequest = new RequestInfo();
    rpcRequest.setInterfaceName(serviceInterface.getName());
    rpcRequest.setMethodName(method.getName());
    rpcRequest.setMethodParameters(args);
    rpcRequest.setRequestId(rpcRequest.getInterfaceName() + "#" + longSeq.getAndIncrement());
    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setMagic(RpcMessage.MAGIC_NUMBER);
    rpcMessage.setMessageType(MessageType.RPC_REQUEST);
    rpcMessage.setContent(rpcRequest);
    return rpcMessage;
  }
}