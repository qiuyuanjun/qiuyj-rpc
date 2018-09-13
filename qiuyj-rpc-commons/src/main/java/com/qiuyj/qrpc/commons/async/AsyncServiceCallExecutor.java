package com.qiuyj.qrpc.commons.async;

import java.util.concurrent.*;

/**
 * @author qiuyj
 * @since 2018-09-10
 */
public class AsyncServiceCallExecutor extends ThreadPoolExecutor implements ExecutorService {

  private static int corePoolSize = 1;

  private static int maxPoolSize = 1;

  private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(20);

  private static long keepAliveTime = 30L;

  private static TimeUnit unit = TimeUnit.SECONDS;

  static {
    // 读取配置文件或者环境变量，初始化各种参数

  }

  public AsyncServiceCallExecutor() {
    super(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
  }
}
