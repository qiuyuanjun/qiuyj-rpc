package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.commons.async.DefaultFuture;
import com.qiuyj.qrpc.commons.protocol.ResponseInfo;
import com.qiuyj.qrpc.server.interceptor.ServiceInvocationInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public abstract class AbstractMessageHandler<T> implements MessageHandler<T> {

  /**
   * 拦截器
   */
  private List<ServiceInvocationInterceptor> interceptors;

  /**
   * 异步执行线程池
   */
  private ExecutorService asyncExecutor;

  protected AbstractMessageHandler(List<ServiceInvocationInterceptor> interceptors, ExecutorService asyncExecutor) {
    this.interceptors = Objects.isNull(interceptors) ? Collections.emptyList() : interceptors;
    this.asyncExecutor = asyncExecutor;
  }

  @Override
  public ResponseInfo handle(T message) {
    // 首先判断是否是异步执行
    if (isAsyncExecute(message)) {
      // 如果是异步执行，那么开启线程池执行对应的服务请求
      DefaultFuture<Object> responseFuture = new DefaultFuture<>(asyncExecutor);
      responseFuture.addListener(f -> sendAsyncResponse(message, f));
      asyncExecutor.execute(() -> {
        try {
          Object result = getResult(message);
          responseFuture.setSuccess(result);
        }
        catch (Exception e) {
          responseFuture.setFailure(e);
        }
      });
      return null;
    }
    else {
      Object result = getResult(message);
      // 发送消息到客户端
      ResponseInfo response = new ResponseInfo();
      response.setResult(result);
      return response;
    }
  }

  /**
   * 得到服务调用的结果
   * @param message 服务请求对象
   * @return 调用结果
   */
  private Object getResult(T message) {
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
    return result;
  }

  /**
   * 异步服务响应，模版方法，交给子类实现
   * @param message 请求消息
   * @param future 已经完成的future对象
   */
  protected void sendAsyncResponse(T message, DefaultFuture future) {
    // do nothing, for subclass
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
