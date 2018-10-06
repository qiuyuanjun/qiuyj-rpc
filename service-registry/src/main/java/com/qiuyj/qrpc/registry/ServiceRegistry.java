package com.qiuyj.qrpc.registry;

import java.io.Closeable;
import java.util.List;

/**
 * 服务注册中心抽象接口
 * @author qiuyj
 * @since 2018-09-23
 */
public interface ServiceRegistry extends Closeable {

  /**
   * 得到某个应用所有已发布的服务提供者
   * @param applicationName 应用名
   * @return 当前应用下所有已经发布的服务接口的{{@code ServiceInstance}集合
   */
  List<ServiceInstance> getProvidersByApplicationName(String applicationName);

  /**
   * 注册服务到服务注册中心
   * @param serviceInstance 服务实例
   */
  void register(ServiceInstance serviceInstance);

  /**
   * 从服务注册中心下线服务
   * @param serviceInstance 服务实例
   */
  void unregister(ServiceInstance serviceInstance);

  /**
   * 关闭服务注册中心
   */
  @Override
  void close();
}
