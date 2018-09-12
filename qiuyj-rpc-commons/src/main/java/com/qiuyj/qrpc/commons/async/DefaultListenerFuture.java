package com.qiuyj.qrpc.commons.async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author qiuyj
 * @since 2018-09-12
 */
@SuppressWarnings("unchecked")
public class DefaultListenerFuture<V> implements ListenableFuture<V> {

  private static final AtomicReferenceFieldUpdater<DefaultListenerFuture, Object> RESULT_UPDATER
      = AtomicReferenceFieldUpdater.newUpdater(DefaultListenerFuture.class, Object.class, "result");

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
  private List<GenericListener<V>> listeners;

  private ExecutorService listenerExecutor;

  @Override
  public void setSuccess(V result) {
    if (setSuccess0(result)) {
      // 通知所有的监听器调用onResultSetted方法

    }
    else {
      throw new IllegalStateException("Result has been already setted.");
    }
  }

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
  public boolean trySetSuccess(V result) {
    if (setSuccess0(result)) {
      // 通知所有的监听器调用onResultSetted方法
      return true;
    }
    return false;
  }

  @Override
  public void setFailure(Throwable failure) {

  }

  @Override
  public boolean trySetFailure(Throwable failure) {
    return false;
  }

  @Override
  public V getNow() {
    if (isDone()) {
      Object result = this.result;
      if (result instanceof Throwable) {
        rethrowThrowable((Throwable) result);
      }
      else if (result != SUCCESS) {
        return (V) result;
      }
    }
    return null;
  }

  /**
   * 将异常分类并重新抛出
   * @param t 异常
   */
  private static void rethrowThrowable(Throwable t) {
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    else if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof InvocationTargetException) {
      rethrowThrowable(((InvocationTargetException) t).getTargetException());
    }
    else {
      throw new UndeclaredThrowableException(t);
    }
  }

  @Override
  public void addListener(GenericListener<V> listener) {
    Objects.requireNonNull(listener, "listener == null.");
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

    // 判断是否已经完成，如果已经完成，那么notify所有的listener
    if (isDone()) {

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
