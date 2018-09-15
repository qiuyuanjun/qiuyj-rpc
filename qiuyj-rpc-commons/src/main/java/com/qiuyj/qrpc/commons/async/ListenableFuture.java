package com.qiuyj.qrpc.commons.async;

import java.util.concurrent.Future;

/**
 * 支持监听器的future
 * @author qiuyj
 * @since 2018-09-12
 */
public interface ListenableFuture<V> extends Future<V> {

  /**
   * 立即获取结果，如果此时还没有完成，那么直接返回{@code null}
   * @return 对应的结果，可能为{@code null}
   */
  V getNow();

  /**
   * 添加监听器
   * @param listener 监听器
   */
  void addListener(GenericFutureListener listener);
}