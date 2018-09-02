package com.qiuyj.qrpc.client;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public interface ConfigurableRpcClient<T> extends RpcClient<T> {

  /**
   * 设置服务接口
   * @param serviceInterface 服务接口
   */
  void setServiceInterface(Class<T> serviceInterface);

  /**
   * 设置延迟初始化服务实例对象，只要调用了该方法，就一定设置为true
   */
  void setLazyInitServiceInstance();
}
