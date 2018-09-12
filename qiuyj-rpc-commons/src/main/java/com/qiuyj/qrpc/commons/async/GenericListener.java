package com.qiuyj.qrpc.commons.async;

/**
 * 通用监听器
 * @author qiuyj
 * @since 2018-09-12
 */
public interface GenericListener<V> {

  /**
   * 在future被设置result的时候调用该方法
   * @param result 结果
   */
  void onResultSetted(V result);
}