package com.qiuyj.qrpc.commons;

/**
 * @author qiuyj
 * @since 2018-06-12
 */
public interface ServiceInstanceProvider {

  /**
   * 默认实现
   */
  ServiceInstanceProvider DEFAULT = new DefaultServiceInstanceProvider();

  /**
   * 根据接口得到对应的实例
   * @param serviceInterface 接口{@code Class}对象
   * @return 接口实例
   */
  <T> T getServiceInstance(Class<? super T> serviceInterface);
}