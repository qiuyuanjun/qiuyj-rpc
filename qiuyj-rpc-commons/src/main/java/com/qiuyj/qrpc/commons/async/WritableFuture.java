package com.qiuyj.qrpc.commons.async;

import java.util.concurrent.Future;

/**
 * 支持对Future进行写操作
 * @author qiuyj
 * @since 2018-09-13
 */
public interface WritableFuture<V> extends Future<V> {

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
  boolean trySuccess(V result);

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
  boolean tryFailure(Throwable failure);
}
