package com.qiuyj.api.client;

import java.io.Closeable;
import java.net.InetAddress;

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

  /**
   * 得到远程服务器的ip地址
   * @return 远程服务器的ip地址
   */
  InetAddress getRemoteAddress();

  /**
   * 得到当前客户端所在的机器的真实ip地址
   * @return 当前机器的真实ip的{{@code InetAddress}对象
   */
  InetAddress getLocalAddress();
}
