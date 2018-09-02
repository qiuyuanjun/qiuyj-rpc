package com.qiuyj.qrpc.server.interceptor;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public abstract class ServiceInvocationInterceptorWithoutResult implements ServiceInvocationInterceptor {

  @Override
  public Object beforeInvoke(Object message) {
    doBeforeInvoke(message);
    return null;
  }

  protected abstract void doBeforeInvoke(Object message);

  @Override
  public void afterInvoke(boolean catchException, Object result) {
    // adaptive
  }
}