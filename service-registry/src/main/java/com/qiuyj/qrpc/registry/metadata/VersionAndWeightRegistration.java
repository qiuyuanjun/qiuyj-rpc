package com.qiuyj.qrpc.registry.metadata;

/**
 * 支持版本号和权重的服务注册实例接口
 * @author qiuyj
 * @since 2018-10-06
 */
public interface VersionAndWeightRegistration extends Registration {

  /**
   * 得到当前服务接口的版本号
   * @return 版本号
   */
  String getVersion();

  /**
   * 得到当前服务接口的权重
   * @return 权重
   */
  int getWeight();
}
