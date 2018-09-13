package com.qiuyj.qrpc.commons.async;

import java.util.concurrent.Future;

/**
 * 支持监听器的future
 * @author qiuyj
 * @since 2018-09-12
 */
public interface ListenableFuture<V> extends Future<V> {

  /**
   * 添加监听器
   * @param listener 监听器
   */
  void addListener(GenericListener<DefaultFuture<?>> listener);
}