package com.qiuyj.api.server;

import java.io.Closeable;
import java.net.InetAddress;

/**
 * 服务器抽象接口
 * @author qiuyj
 * @since 2018-06-12
 */
public interface Server extends Closeable {

  /**
   * 启动服务器，无法启动两次，要启动服务器，必须是处于关闭状态下
   */
  void start();

  /**
   * 优雅的关闭服务器，调用此方法，服务器必须处于启动状态
   */
  @Override
  void close();

  /**
   * 得到当前服务器的端口
   * @implNote 端口不能小于1024
   * @return 服务器所绑定的端口
   */
  int getPort();

  /**
   * 得到当前服务器的真实可用的本地ip地址
   * @return {@code InetAddress}对象
   */
  InetAddress getLocalAddress();

  /**
   * 判断服务器的状态
   * @return 如果服务器正在运行，那么返回{@code true}，否则返回{@code false}
   */
  boolean isRunning();

}
