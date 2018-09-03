package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.commons.instantiation.ObjectFactory;
import com.qiuyj.qrpc.commons.instantiation.ServiceInstanceProvider;

/**
 * @author qiuyj
 * @since 2018-06-13
 */
public interface ConfigurableRpcServer extends RpcServer {

  String PACKAGE_SEPERATOR = ",; \t";

  void setServiceInstanceProvider(ServiceInstanceProvider serviceInstanceProvider);

  void setObjectFactory(ObjectFactory objectFactory);

  /**
   * 注册需要暴露的服务
   * @param serviceInterface 服务接口
   * @param instance 服务实例
   */
  <T> void addServiceToExport(Class<? super T> serviceInterface, T instance);

  /**
   * 设置服务扫描路径
   * @param serviecPackage 服务扫描的第一个路径
   * @param more 更多路径，可选
   */
  void setServicePackageToScan(String serviecPackage, String... more);

  /**
   * 设置端口
   * @param port 要设置的端口值
   */
  void setPort(int port);
}
