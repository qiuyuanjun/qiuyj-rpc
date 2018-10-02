package com.qiuyj.qrpc.client.proxy.jdk;

import com.qiuyj.api.Connection;
import com.qiuyj.qrpc.client.AsyncContext;
import com.qiuyj.qrpc.client.requestid.LongSequenceRequestId;
import com.qiuyj.qrpc.client.requestid.RequestId;
import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import com.qiuyj.qrpc.codec.protocol.MessageType;
import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.codec.protocol.ResponseInfo;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author qiuyj
 * @since 2018-09-01
 */
public class ServiceInstanceJdkProxyHandler implements InvocationHandler {

  /**
   * id生成对象，所有客户端共享一个
   */
  private static RequestId requestId = new LongSequenceRequestId();

  /**
   * 服务接口
   */
  private Class<?> serviceInterface;

  /**
   * 客户端和服务器端之间的连接对象
   */
  private Connection connection;

  public ServiceInstanceJdkProxyHandler(Class<?> serviceInterface, Connection connection) {
    this.serviceInterface = serviceInterface;
    this.connection = connection;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 通过实验发现，如果是调用Object.notify,Object.notifyAll,Object.wait方法
    // 那么将不会执行当前的当前的动态代理的invoke逻辑

    /*String methodSign = MethodSignUtils.getMethodSign(method);
    // 判断当前调用的方法是否是不可执行的Object方法
    if (ObjectMethods.INSTANCE.isNotExecutableObjectMethod(method)) {
      throw new IllegalStateException("Method " + method.getName() + " that in rpc environment is not executable.");
    }
    // 判断当前调用的方法是否是Object.getClass方法
    else if (ObjectMethods.INSTANCE.isNotExecutableObjectMethod(methodSign)) {
      // 如果是，那么直接将接口返回
      return serviceInterface;
    }*/

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

  /**
   * 得到rpc调用的请求报文
   * @param serviceInterface 服务接口
   * @param method 调用的方法
   * @param args 方法参数
   * @return rpc请求报文
   */
  private static RpcMessage getRequestRpcMessage(Class<?> serviceInterface, Method method, Object[] args) {
    RequestInfo rpcRequest = new RequestInfo();
    rpcRequest.setInterfaceName(serviceInterface.getName());
    rpcRequest.setMethodName(method.getName());
    rpcRequest.setMethodParameters(args);
    rpcRequest.setRequestId(requestId.nextRequestId());

    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setMagic(RpcMessage.MAGIC_NUMBER);
    // 判断当前请求是否是异步请求，默认为同步请求
    MessageType requestType = MessageType.RPC_REQUEST;
    if (method.isAnnotationPresent(RpcMethod.class)) {
      RpcMethod rpcMethod = method.getAnnotation(RpcMethod.class);
      if (rpcMethod.async()) {
        requestType = MessageType.ASYNC_REQUEST;
        // 往AsyncContext里面初始化异步调用的对应的参数
        AsyncContext.initAsyncCall(rpcRequest.getRequestId());
      }
    }
    rpcMessage.setMessageType(requestType);
    rpcMessage.setContent(rpcRequest);

    return rpcMessage;
  }
}
