package com.qiuyj.qrpc.registry;

import java.util.List;

/**
 * 服务注册中心抽象接口
 * @author qiuyj
 * @since 2018-09-23
 */
public interface ServiceRegistry {

  /**
   * 得到所有已经注册的服务
   * @return 所有的{{@code ServiceInstance}
   */
  List<ServiceInstance> registeredServiceInstances();

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
}