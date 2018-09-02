package com.qiuyj.api;

import java.io.Closeable;

/**
 * @author qiuyj
 * @since 2018-08-28
 */
public interface Connection extends Closeable {

  /**
   * 默认的空连接
   */
  Connection EMPTY_CONNECTION = new Connection() {

    @Override
    public Object send(Object message) {
      // do nothing and return null
      return null;
    }

    @Override
    public void close() {
      // do nothing
    }
  };

  /**
   * 向远程服务器发送数据
   * @param message 要发送的数据
   * @return 服务器端返回的数据
   */
  Object send(Object message);

  /**
   * 关闭与远程服务器的链接
   */
  @Override
  void close();
}
