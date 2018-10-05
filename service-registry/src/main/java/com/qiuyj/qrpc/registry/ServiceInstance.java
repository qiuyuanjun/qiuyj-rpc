package com.qiuyj.qrpc.registry;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-09-23
 */
public class ServiceInstance {

  /**
   * 空的{@code ServiceInstnace}对象
   * 用于服务注册的时候，由于注册线程会一直阻塞
   * 所以在服务注册中心关闭的时候，注册线程会一直阻塞
   * 所以关闭的时候需要手动往阻塞队列里面加入这个对象从而唤醒被阻塞的线程
   */
  static final ServiceInstance EMPTY_SERVICE_INSTANCE = new ServiceInstance();

  /** 服务名（一般指接口全限定类名） */
  private String serviceName;

  /** 服务版本 */
  private String version;

  /** 服务所处服务器的ip */
  private String ip;

  /** 服务所处的服务器的port */
  private int port;

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceInstance that = (ServiceInstance) o;
    return port == that.port &&
        Objects.equals(serviceName, that.serviceName) &&
        Objects.equals(version, that.version) &&
        Objects.equals(ip, that.ip);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceName, version, ip, port);
  }
}
