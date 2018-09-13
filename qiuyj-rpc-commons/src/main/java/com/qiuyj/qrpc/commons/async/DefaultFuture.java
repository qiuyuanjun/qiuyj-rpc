package com.qiuyj.qrpc.commons.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * {@code Future}默认实现，参考了netty的实现
 * @author qiuyj
 * @since 2018-09-12
 */
public class DefaultFuture<V> implements ListenableFuture<V>, WritableFuture<V> {

  private static final AtomicReferenceFieldUpdater<DefaultFuture, Object> RESULT_UPDATER
      = AtomicReferenceFieldUpdater.newUpdater(DefaultFuture.class, Object.class, "result");

  /**
   * 如果被设置的值为null，那么通过该对象代替
   */
  private static final Object SUCCESS = new Object();

  /**
   * 计算结果，可能是一个异常
   */
  private volatile Object result;

  /**
   * 当前{@code Future}的所有的监听器
   */
  private List<GenericListener<?>> listeners;

  /**
   * 执行所有listener的complete方法的线程池
   */
  private ExecutorService listenerExecutor;

  public DefaultFuture() {
//    this(null);
    // no-op
  }

  public DefaultFuture(ExecutorService listenerExecutor) {
    this.listenerExecutor = listenerExecutor;
  }

  @Override
  public void setSuccess(V result) {
    if (setSuccess0(result)) {
      // 通知所有的监听器调用complete方法

    }
    else {
      throw new IllegalStateException("Result has been already setted.");
    }
  }

  /**
   * 通过cas算法线程安全的设置result值，并且唤醒全部由于调用了get方法而阻塞的线程
   * @param result 值
   * @return 如果设置成功，一般是原来的result字段为null，那么返回{@code true}，否则返回{@code false}
   */
  private boolean setSuccess0(V result) {
    if (RESULT_UPDATER.compareAndSet(this, null, Objects.isNull(result) ? SUCCESS : result)) {
      // 唤醒所有被阻塞的线程
      synchronized (this) {
        notifyAll();
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean trySuccess(V result) {
    if (setSuccess0(result)) {
      // 通知所有的监听器调用complete方法
      return true;
    }
    return false;
  }

  @Override
  public void setFailure(Throwable failure) {

  }

  @Override
  public boolean tryFailure(Throwable failure) {
    return false;
  }

  @Override
  public void addListener(GenericListener<DefaultFuture<?>> listener) {
    Objects.requireNonNull(listener, "listener == null.");
    // 判断是否已经完成，如果已经完成，那么notify当前的listener
    if (isDone()) {
      listener.complete(this);
    }
    else {
      if (Objects.isNull(listeners)) {
        synchronized (this) {
          if (Objects.isNull(listeners)) {
            listeners = new ArrayList<>();
          }
        }
      }
      synchronized (listeners) {
        listeners.add(listener);
      }
    }
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return Objects.nonNull(result);
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    return null;
  }

  @Override
  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return null;
  }

}