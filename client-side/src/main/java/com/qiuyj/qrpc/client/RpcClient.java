package com.qiuyj.qrpc.client;

import com.qiuyj.api.client.ConfigurableClient;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public interface RpcClient<T> extends ConfigurableClient {

  /**
   * 得到对应的服务接口
   * @return 服务接口
   */
  Class<T> getServiceInterface();

  /**
   * 得到服务实例
   * @return 服务实例
   */
  T getServiceInstance();
}
