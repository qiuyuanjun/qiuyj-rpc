package com.qiuyj.qrpc.commons.async;

import java.util.concurrent.Future;

/**
 * 支持监听器的future
 * @author qiuyj
 * @since 2018-09-12
 */
public interface ListenableFuture<V> extends Future<V> {

  /**
   * 设置结果，如果已经设置了，那么应该抛出一个异常
   * @param result 结果
   */
  void setSuccess(V result);

  /**
   * 尝试设置结果，如果设置成功，那么返回true，如果已经设置了结果，那么返回false
   * @param result 结果
   * @return 如果之前已经设置了结果，那么返回{@code false}，否则返回{@code true}
   */
  boolean trySetSuccess(V result);

  /**
   * 设置错误，如果当前的{@code Future}处于已经完成的状态，那么抛出异常
   * @param failure 错误对象
   */
  void setFailure(Throwable failure);

  /**
   * 尝试设置错误，如果当前的{@code Future}处于已经完成的状态，那么返回{@code false}，否则返回{@code true}
   * @param failure 错误对象
   * @return 如果当前的{@code Future}处于已经完成的状态，那么返回{@code false}，否则返回{@code true}
   */
  boolean trySetFailure(Throwable failure);

  /**
   * 立即得到结果，如果当前{@code Future}处于未完成状态，那么立即返回{@code null}，否则返回结果
   * 如果结果是一个异常对象，那么抛出异常
   * @return 结果
   */
  V getNow();

  /**
   * 添加监听器
   * @param listener 监听器
   */
  void addListener(GenericListener<V> listener);
}