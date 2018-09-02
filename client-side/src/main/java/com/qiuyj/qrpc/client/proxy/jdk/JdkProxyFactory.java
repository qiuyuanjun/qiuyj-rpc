package com.qiuyj.qrpc.client.proxy.jdk;

import com.qiuyj.api.Connection;
import com.qiuyj.qrpc.client.proxy.AbstractProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @author qiuyj
 * @since 2018-09-01
 */
public class JdkProxyFactory extends AbstractProxyFactory {

  @Override
  protected Object doGetProxy(Class<?> interfaceCls, Connection connection) {
    return Proxy.newProxyInstance(JdkProxyFactory.class.getClassLoader(),
        new Class<?>[] {interfaceCls},
        new ServiceInstanceJdkProxyHandler(interfaceCls, connection));
  }
}
