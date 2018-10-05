package com.qiuyj.qrpc.registry;

import com.qiuyj.qrpc.registry.zookeeper.ZookeeperServiceRegistry;

/**
 * @author qiuyj
 * @since 2018-10-05
 */
public class ServiceRegistryFactory {

  public static ServiceRegistry getServiceRegistry() {
    // TODO: 从配置文件里面读取具体实例化那个注册中心
    return new ZookeeperServiceRegistry();
  }
}
