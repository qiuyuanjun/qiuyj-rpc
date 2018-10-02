package com.qiuyj.qrpc.server.interceptor;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public interface ServiceInvocationInterceptor {

  /**
   * 执行调用的服务方法之前执行的逻辑，如果有返回值，那么将不会执行服务调用的方法，而直接返回
   * @param message rpc请求消息
   * @return {@code null}或者一个响应对象
   */
  Object beforeInvoke(Object message);

  /**
   * 执行完服务方法之后的回调方法
   * @param catchException 执行服务方法的期间是否发生异常
   * @param result 执行结果
   */
  void afterInvoke(boolean catchException, Object result);
}
