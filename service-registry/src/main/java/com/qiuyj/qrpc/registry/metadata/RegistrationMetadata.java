package com.qiuyj.qrpc.registry.metadata;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务注册中心所接受的注册的源信息
 * @author qiuyj
 * @since 2018-10-06
 */
public class RegistrationMetadata implements Registration, Serializable {

  private static final long serialVersionUID = 1621949805514612954L;

  /** 服务接口的全限定名 */
  private String name;

  /** ip地址 */
  private String ipAddress;

  /** 端口号 */
  private int port;

  public void setName(String name) {
    this.name = name;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getIpAddress() {
    return ipAddress;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegistrationMetadata)) {
      return false;
    }
    RegistrationMetadata that = (RegistrationMetadata) o;
    return port == that.port &&
        Objects.equals(name, that.name) &&
        Objects.equals(ipAddress, that.ipAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, ipAddress, port);
  }
}
