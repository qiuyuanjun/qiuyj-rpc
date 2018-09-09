package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.qrpc.commons.protocol.ResponseInfo;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.server.interceptor.ServiceInvocationInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public abstract class AbstractMessageHandler<T> implements MessageHandler<T> {

  private List<ServiceInvocationInterceptor> interceptors;

  protected AbstractMessageHandler(List<ServiceInvocationInterceptor> interceptors) {
    this.interceptors = Objects.isNull(interceptors) ? Collections.emptyList() : interceptors;
  }

  @Override
  public ResponseInfo handle(T message) {
    // 首先判断是否是异步执行
    if (isAsyncExecute(message)) {
      // 如果是异步执行，那么开启线程池执行对应的服务请求
      return null;
    }
    else {
      Object result = null;
      boolean catchException = false;
      try {
        result = beforeHandleMessage(message);
        if (Objects.isNull(result)) {
          result = doHandle(message);
        }
      }
      catch (Throwable e) {
        catchException = true;
        if (e instanceof RpcException) {
          throw (RpcException) e;
        }
        result = e;
      }
      finally {
        afterCompleteHandleMessage(catchException, result);
      }
      // 发送消息到客户端
      ResponseInfo response = new ResponseInfo();
      response.setResult(result);
      return response;
    }
  }

  protected Object beforeHandleMessage(T message) {
    Object result = null;
    for (ServiceInvocationInterceptor interceptor : interceptors) {
      result = interceptor.beforeInvoke(message);
      if (Objects.nonNull(result)) {
        break;
      }
    }
    return result;
  }

  protected void afterCompleteHandleMessage(boolean catchException, Object result) {
    for (ServiceInvocationInterceptor interceptor : interceptors) {
      interceptor.afterInvoke(catchException, result);
    }
  }

  /**
   * 具体的处理消息的方法，交给子类实现
   * @param message 要处理的消息
   * @return 处理的结果
   */
  protected abstract Object doHandle(T message);

  /**
   * 判断一个当前的请求是否是异步执行
   * @param message 当前的请求体
   * @return 如果是异步执行，那么返回{@code true}，否则返回{@code false}
   */
  protected abstract boolean isAsyncExecute(T message);
}
