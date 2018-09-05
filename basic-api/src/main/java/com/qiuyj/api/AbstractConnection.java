package com.qiuyj.api;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author qiuyj
 * @since 2018-08-31
 */
public abstract class AbstractConnection implements Connection {

  private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

  /** 检测链路是否是空闲的定时器 */
  private static ScheduledExecutorService channelIdleStateChecker = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);

  /** 上一次写数据的时间 */
  private long lastWriteTime;

  /** 最大的空闲时间，默认30秒 */
  private long maxWriterIdleNanoTime = TimeUnit.SECONDS.toNanos(30L);

  /**
   * 心跳标志，如果发送这个，意味着子类需要发送心跳包
   */
  protected Object heartbeat = new Object();

  protected AbstractConnection() {
//    initialize();
  }

  @Override
  public Object send(Object message) {
    Object result = doSend(message);
    lastWriteTime = ticksInNanos();
    return result;
  }

  /**
   * 具体的返送数据给远程服务器的实现方法，交给对应的子类实现
   * @param message 发送的数据
   */
  protected abstract Object doSend(Object message);

  static long ticksInNanos() {
    return System.nanoTime();
  }

  /**
   * 初始化空闲链路定时任务
   * @apiNote 此方法需要根据Socket初始化的状态调用，一般是socket初始化之后调用
   */
  protected void initialize() {
    lastWriteTime = ticksInNanos();
    maxWriterIdleNanoTime = maxWriterIdleNanoTime > 0 ? Math.max(maxWriterIdleNanoTime, MIN_TIMEOUT_NANOS) : MIN_TIMEOUT_NANOS;
    schedule(new WriterIdleTimeoutTask(), maxWriterIdleNanoTime);
  }

  /**
   * 开启检测链路是否是空闲的定时任务
   * @param task 任务
   * @param delay 时延
   */
  private static void schedule(Runnable task, long delay) {
    channelIdleStateChecker.schedule(task, delay, TimeUnit.NANOSECONDS);
  }

  private class WriterIdleTimeoutTask implements Runnable {

    @Override
    public void run() {
      long nextDelay = AbstractConnection.this.maxWriterIdleNanoTime - //
                (ticksInNanos() - AbstractConnection.this.lastWriteTime);
      if (nextDelay <= 0) {
        // 写超时
        // 发送心跳标志，提醒子类发送心跳包
        AbstractConnection.this.send(heartbeat);
        // 重置定时器
        AbstractConnection.schedule(this, AbstractConnection.this.maxWriterIdleNanoTime);
      }
      else {
        AbstractConnection.schedule(this, nextDelay);
      }
    }
  }
}