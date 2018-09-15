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
@SuppressWarnings("unchecked")
public class DefaultFuture<V> implements ListenableFuture<V>, WritableFuture<V> {

  private static final AtomicReferenceFieldUpdater<DefaultFuture, Object> RESULT_UPDATER
      = AtomicReferenceFieldUpdater.newUpdater(DefaultFuture.class, Object.class, "result");

  /**
   * 如果调用setSuccess方法，而传入的参数为null，那么通过该对象代替
   */
  private static final Object NULL_SUCCESS = new Object();

  /**
   * 如果调用setFailure方法，而传入的参数为null，那么用这个代替
   */
  private static final CauseHolder NULL_SUCCESS_CAUSEHOLDER = new CauseHolder(new NullPointerException("Null result exception."));

  /**
   * 计算结果，可能是一个异常
   */
  private volatile Object result;

  /**
   * 当前{@code Future}的所有的监听器
   */
  private List<GenericFutureListener> listeners;

  /**
   * 执行所有listener的complete方法的线程池
   */
  private ExecutorService listenerExecutor;

  /**
   * 当前被阻塞的线程数
   */
  private int waitThreads;

  public DefaultFuture() {
//    this(null);
    // no-op
  }

  public DefaultFuture(ExecutorService listenerExecutor) {
    this.listenerExecutor = listenerExecutor;
  }

  @Override
  public void setSuccess(V success) {
    if (setSuccess0(success)) {
      // 通知所有的监听器调用complete方法
      notifyListeners(this, listenerExecutor, listeners);
    }
    else {
      throw new IllegalStateException("Result has been already set.");
    }
  }

  /**
   * 通过cas算法线程安全的设置result值，并且唤醒全部由于调用了get方法而阻塞的线程
   * @param success 值
   * @return 如果设置成功，一般是原来的result字段为null，那么返回{@code true}，否则返回{@code false}
   */
  private boolean setSuccess0(V success) {
    Object result = Objects.isNull(success) ? NULL_SUCCESS : success;
    if (RESULT_UPDATER.compareAndSet(this, null, result)) {
      notifyAll0();
      return true;
    }
    return false;
  }

  @Override
  public boolean trySuccess(V result) {
    if (setSuccess0(result)) {
      // 通知所有的监听器调用complete方法
      notifyListeners(this, listenerExecutor, listeners);
      return true;
    }
    return false;
  }

  @Override
  public void setFailure(Throwable failure) {
    if (setFailure0(failure)) {
      notifyListeners(this, listenerExecutor, listeners);
    }
    else {
      throw new IllegalStateException("Result has been already set.");
    }
  }

  /**
   * 线程安全的将result设置为一个异常对象
   * @param failure 异常对象
   * @return 如果调用该方法前，result为{@code null}，那么返回{@code true}，否则返回{@code false}
   */
  private boolean setFailure0(Throwable failure) {
    CauseHolder result = Objects.isNull(failure) ? NULL_SUCCESS_CAUSEHOLDER : new CauseHolder(failure);
    if (RESULT_UPDATER.compareAndSet(this, null, result)) {
      notifyAll0();
      return true;
    }
    return false;
  }

  /**
   * 唤醒所有被阻塞的线程
   */
  private void notifyAll0() {
    if (waitThreads > 0) {
      synchronized (this) {
        notifyAll();
      }
    }
  }

  @Override
  public boolean tryFailure(Throwable failure) {
    if (setFailure0(failure)) {
      notifyListeners(this, listenerExecutor, listeners);
      return true;
    }
    return false;
  }

  private static void notifyListeners(DefaultFuture<?> future, ExecutorService executor, List<GenericFutureListener> listeners) {
    if (Objects.nonNull(listeners)) {
      if (Objects.nonNull(executor)) {
        executor.execute(() -> notifyListeners0(future, listeners));
      }
      else {
        notifyListeners0(future, listeners);
      }
    }
  }

  private static void notifyListeners0(DefaultFuture<?> future, List<GenericFutureListener> listeners) {
    for (GenericFutureListener listener : listeners) {
      listener.onCompletion(future);
    }
  }

  @Override
  public void addListener(GenericFutureListener listener) {
    Objects.requireNonNull(listener, "listener == null.");
    // 判断是否已经完成，如果已经完成，那么notify当前的listener
    if (isDone()) {
      listener.onCompletion(this);
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
    // 不支持cancel操作
    throw new UnsupportedOperationException("Unsupport cancel operation.");
  }

  @Override
  public boolean isCancelled() {
    // 不支持cancel操作，那么该方法永远返回false
    return false;
  }

  @Override
  public boolean isDone() {
    return Objects.nonNull(result);
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    if (!isDone()) {
      awaitDone();
    }
    return report();
  }

  /**
   * 一直阻塞当前的线程，直到另外一条线程调用{@link #setSuccess(Object)}或者{@link #trySuccess(Object)}
   * 或者{@link #setFailure(Throwable)}或者{@link #tryFailure(Throwable)}方法
   * @throws InterruptedException 如果在等待过程中，当前线程被中断，那么抛出该异常
   */
  private void awaitDone() throws InterruptedException {
    if (isDone()) {
      return;
    }
    // 如果此时，线程被中断，那么直接抛出异常
    else if (Thread.interrupted()) {
      throw new InterruptedException(toString());
    }
    synchronized (this) {
      while (!isDone()) {
        waitThreads++;
        try {
          wait();
        }
        finally {
          waitThreads--;
        }
      }
    }
  }

  /**
   * 根据result的值，做出对应的响应，如果值为{@link #NULL_SUCCESS},那么返回{@code null}
   * 如果值为{@link CauseHolder}类型，那么抛出{@link ExecutionException}
   * 否则返回result所代表的值
   * @return 得到对应的值
   * @throws ExecutionException 如果在计算结果的过程中，得到的是一个异常对象，那么抛出该异常
   */
  private V report() throws ExecutionException {
    Object result = this.result;
    if (result == NULL_SUCCESS) {
      return null;
    }
    else if (result instanceof CauseHolder) {
      CauseHolder cause = (CauseHolder) result;
      throw new ExecutionException(cause.t);
    }
    else {
      return (V) result;
    }
  }

  @Override
  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (awaitNanos(unit.toNanos(timeout))) {
      return get();
    }
    throw new TimeoutException();
  }

  private boolean awaitNanos(long nanos) throws InterruptedException {
    // 如果已经完成，直接返回true
    if (isDone()) {
      return true;
    }
    // 如果传入过来的等待时间小于等于0，那么立即判断是否设置了结果
    if (nanos <= 0) {
      return isDone();
    }
    // 如果线程被中断，那么抛出被中断异常
    if (Thread.interrupted()) {
      throw new InterruptedException(toString());
    }
    long startTime = System.nanoTime(),
         waitTime = nanos;
    for (;;) {
      synchronized (this) {
        if (isDone()) {
          return true;
        }
        waitThreads++;
        try {
          wait(waitTime / 1000000, (int) waitTime % 1000000);
        }
        finally {
          waitThreads--;
        }
        if (isDone()) {
          return true;
        }
        waitTime = nanos - (System.nanoTime() - startTime);
        if (waitTime <= 0) {
          return isDone();
        }
      }
    }
  }

  @Override
  public String toString() {
    return toStringBuilder().toString();
  }

  protected StringBuilder toStringBuilder() {
    StringBuilder buf = new StringBuilder(64)
        .append(this.getClass().getSimpleName())
        .append('@')
        .append(Integer.toHexString(hashCode()));

    Object result = this.result;
    if (result == NULL_SUCCESS) {
      buf.append("(success)");
    } else if (result instanceof CauseHolder) {
      buf.append("(failure: ")
          .append(((CauseHolder) result).t)
          .append(')');
    } else if (result != null) {
      buf.append("(success: ")
          .append(result)
          .append(')');
    } else {
      buf.append("(incomplete)");
    }

    return buf;
  }

  private static final class CauseHolder {

    private final Throwable t;

    private CauseHolder(Throwable t) {
      this.t = t;
    }
  }

}