package com.qiuyj.qrpc.registry;

import com.qiuyj.qrpc.registry.metadata.VersionAndWeightRegistration;
import com.qiuyj.qrpc.registry.metadata.VersionAndWeightRegistrationMetadata;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-09-23
 */
public class ServiceInstance extends VersionAndWeightRegistrationMetadata {

  private static final long serialVersionUID = -8411208890926458740L;

  /**
   * 空的{@code ServiceInstnace}对象
   * 用于服务注册的时候，由于注册线程会一直阻塞
   * 所以在服务注册中心关闭的时候，注册线程会一直阻塞
   * 所以关闭的时候需要手动往阻塞队列里面加入这个对象从而唤醒被阻塞的线程
   */
  static final ServiceInstance EMPTY_SERVICE_INSTANCE = new ServiceInstance();

  /** 当前服务接口所属的应用名称 */
  private String applicationName;

  public ServiceInstance() {
    // empty body
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ServiceInstance)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ServiceInstance that = (ServiceInstance) o;
    return Objects.equals(applicationName, that.applicationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), applicationName);
  }

  // -- for internal useage --
  // @see {@link AbstractServiceRegistry}
  ServiceInstance(VersionAndWeightRegistration registration, String applicationName) {
    setWeight(registration.getWeight());
    setVersion(registration.getVersion());
    setName(registration.getName());
    setIpAddress(registration.getIpAddress());
    setPort(registration.getPort());
    setApplicationName(applicationName);
  }

}
