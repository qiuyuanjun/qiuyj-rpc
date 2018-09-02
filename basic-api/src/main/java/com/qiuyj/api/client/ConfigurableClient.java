package com.qiuyj.api.client;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public interface ConfigurableClient extends Client {

  /**
   * 当客户端和服务器端之间连接失败的时候，尝试重新连接的最大次数
   * @param maxRetry 重新尝试连接的最大次数
   */
  void setMaxRetryWhenFailedToConnect(int maxRetry);
}
