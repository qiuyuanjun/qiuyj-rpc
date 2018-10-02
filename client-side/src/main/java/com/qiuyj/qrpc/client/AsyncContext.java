package com.qiuyj.qrpc.client;

import com.qiuyj.qrpc.commons.async.DefaultFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiuyj
 * @since 2018-09-19
 */
public class AsyncContext {

  /** 每条线程的future队列的最大长度，如果超过该长度，那么从0重新开始 */
  private static final int QUEUE_MAXLENGTH_OF_FUTURES_PER_THREAD = 1024;

  /** requestId -> thread */
  private static Map<String, Thread> threadMap = new HashMap<>(128);

  /** thread -> index */
  private static Map<Thread, Integer> currentThreadFutureIndex = new ConcurrentHashMap<>(64);

  /** thread -> futures */
  private static Map<Thread, DefaultFuture<Object>[]> futureMap = new ConcurrentHashMap<>(64);

  /**
   * 得到当前线程有关的异步调用的Future对象
   * @return {@code DefaultFuture}对象
   */
  public static DefaultFuture<Object> getFuture() {
    Thread currentThread = Thread.currentThread();
    Integer index = currentThreadFutureIndex.get(currentThread);
    if (Objects.isNull(index)) {
      throw new IllegalStateException("Not initialize.");
    }
    DefaultFuture<Object>[] defaultFutures = futureMap.get(currentThread);
    if (Objects.isNull(defaultFutures)) {
      throw new IllegalStateException("Not initialize.");
    }
    return defaultFutures[index];
  }

  /**
   * 异步接收从服务器端返回的数据,并设置Future
   * @param requestId 异步请求id
   * @param future future对象
   */
  public static void setFuture(String requestId, DefaultFuture<Object> future) {
    Thread t = threadMap.remove(requestId);
    if (Objects.isNull(t)) {
      throw new IllegalStateException("Not initialize.");
    }
    Integer newIdx = currentThreadFutureIndex.computeIfPresent(t, (thread, oldIdx) -> {
      int newValue = oldIdx + 1;
      if (newValue >= QUEUE_MAXLENGTH_OF_FUTURES_PER_THREAD) {
        newValue = 0;
      }
      return newValue;
    });
    if (Objects.isNull(newIdx)) {
      throw new IllegalStateException("Not initialize.");
    }
    futureMap.get(t)[newIdx] = future;
  }

  /**
   * 初始化和当前线程相关的异步调用的一些变量
   * @apiNote 该方法为内部方法，外部用户请不要调用该方法，一定不要调用这个方法
   * @param requestId 请求id
   */
  @SuppressWarnings("unchecked")
  public static void initAsyncCall(String requestId) {
    Thread currentThread = Thread.currentThread();
    threadMap.put(requestId, currentThread);
    currentThreadFutureIndex.putIfAbsent(currentThread, -1);
    futureMap.computeIfAbsent(currentThread, t -> new DefaultFuture[QUEUE_MAXLENGTH_OF_FUTURES_PER_THREAD]);
  }
}
