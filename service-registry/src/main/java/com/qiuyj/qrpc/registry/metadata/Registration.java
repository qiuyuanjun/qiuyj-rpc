package com.qiuyj.qrpc.registry.metadata;

/**
 * @author qiuyj
 * @since 2018-10-06
 */
public interface Registration {

  /**
   * 得到注册中心所注册的服务的全限定名称
   * @return 服务名称，一般是指注册的服务接口的全限定名
   */
  String getName();

  /**
   * 得到注册中心所注册的服务所在机器的ip地址
   * @return ip address
   */
  String getIpAddress();

  /**
   * 得到注册中心所注册的服务所在机器的端口号
   * @return 端口号
   */
  int getPort();
}
