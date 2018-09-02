package com.qiuyj.api.client;

import java.io.Closeable;

/**
 * @author qiuyj
 * @since 2018-08-28
 */
public interface Client extends Closeable {

  /**
   * 连接远程服务器
   */
  void connect();

  /**
   * 重新连接远程服务器
   */
  void reconnect();

  /**
   * 关闭和远程服务器之间的连接
   */
  @Override
  void close();
}
