package com.qiuyj.qrpc.registry;

/**
 * @author qiuyj
 * @since 2018-09-23
 */
public class ServiceInstance {

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
}
