package com.qiuyj.qrpc.commons.async;

import java.util.EventListener;

/**
 * 通用监听器
 * @author qiuyj
 * @since 2018-09-12
 */
public interface GenericListener<F extends DefaultFuture<?>> extends EventListener {

  /**
   * 当{@code Future}的result被设置了值之后调用的方法
   * @param future 当前future
   */
  void complete(F future);
}