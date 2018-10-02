package com.qiuyj.qrpc.client.proxy;

import com.qiuyj.api.Connection;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-09-01
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

  @Override
  public Object getProxy(Class<?> interfaceCls, Connection connection) {
    Objects.requireNonNull(interfaceCls);
    return doGetProxy(interfaceCls, connection);
  }

  protected abstract Object doGetProxy(Class<?> interfaceCls, Connection connection);
}
