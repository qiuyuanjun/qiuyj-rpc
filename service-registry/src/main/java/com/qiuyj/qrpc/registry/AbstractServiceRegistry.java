package com.qiuyj.qrpc.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiuyj
 * @since 2018-09-23
 */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

  /** 所有已经注册了的服务的{@code ServiceInstance}对象 */
  private List<ServiceInstance> serviceInstances;

  protected AbstractServiceRegistry() {
    // 从配置文件读取注册中心的host和port
    String host = "";
    int port = 2181;
    // 连接服务注册中心
    connect(host, port);
    serviceInstances = new ArrayList<>();
  }

  /**
   * 连接服务注册中心，交给具体的子类处理
   * @param host 服务注册中心的ip字符串
   * @param port 服务注册中心的端口
   */
  protected abstract void connect(String host, int port);

  @Override
  public List<ServiceInstance> registeredServiceInstances() {
    return serviceInstances;
  }

  @Override
  public void register(ServiceInstance serviceInstance) {

  }

  @Override
  public void unregister(ServiceInstance serviceInstance) {

  }
}
