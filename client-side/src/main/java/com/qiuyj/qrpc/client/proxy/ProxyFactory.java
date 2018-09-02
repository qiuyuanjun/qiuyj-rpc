package com.qiuyj.qrpc.client.proxy;

import com.qiuyj.api.Connection;

/**
 * @author qiuyj
 * @since 2018-09-01
 */
public interface ProxyFactory {

  /**
   * 得到对应的服务接口的代理对象
   * @param interfaceCls 服务接口
   * @param connection 客户端与服务器端之间的连接对象
   * @return 对应的代理对象
   */
  Object getProxy(Class<?> interfaceCls, Connection connection);
}
