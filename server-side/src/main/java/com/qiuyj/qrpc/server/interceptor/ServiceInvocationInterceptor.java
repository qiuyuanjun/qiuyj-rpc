package com.qiuyj.qrpc.server.interceptor;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public interface ServiceInvocationInterceptor {

  Object beforeInvoke(Object message);

  void afterInvoke(boolean catchException, Object result);
}
