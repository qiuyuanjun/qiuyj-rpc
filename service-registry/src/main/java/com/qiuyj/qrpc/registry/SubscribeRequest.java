package com.qiuyj.qrpc.registry;

/**
 * 订阅请求（主要用于客户端）
 * @author qiuyj
 * @since 2018-10-08
 */
public class SubscribeRequest {

  private String applicationName;

  private String version;

  private String name;

  public SubscribeRequest() {
    // empty body
  }

  public SubscribeRequest(String applicationName, String version, String name) {
    this.applicationName = applicationName;
    this.version = version;
    this.name = name;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
