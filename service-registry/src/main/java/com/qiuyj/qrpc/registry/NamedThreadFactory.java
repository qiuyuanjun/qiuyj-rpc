package com.qiuyj.qrpc.registry;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认的服务注册中心的线程工厂
 * @author qiuyj
 * @since 2018-10-04
 */
public class NamedThreadFactory implements ThreadFactory {

  /** 默认的线程前缀 */
  private static final String DEFAULT_PREFIX = "default";

  /** 线程后缀下标 */
  private final static AtomicInteger idx = new AtomicInteger(0);

  private final String prefix;

  public NamedThreadFactory() {
    this(null);
  }

  public NamedThreadFactory(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    String name = Objects.isNull(prefix) ? getThreadName(DEFAULT_PREFIX) : getThreadName(prefix);
    return newThread(r, name);
  }

  /**
   * 提供一个对每个线程自定义名称的方法
   * @param r {@code Runnable}对象
   * @param name 线程名称
   * @return 创建的{{@code Thread}对象
   */
  public Thread newThread(Runnable r, String name) {
    return new Thread(r, name);
  }

  private static String getThreadName(String prefix) {
    return prefix + "-" + idx.getAndIncrement();
  }
}
