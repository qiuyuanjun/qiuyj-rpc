package com.qiuyj.qrpc.commons.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author qiuyj
 * @since 2018-09-10
 */
public class AsyncServiceCallExecutor extends ThreadPoolExecutor implements ExecutorService {

  private static int corePoolSize;

  private static int maxPoolSize;

  private static BlockingQueue<Runnable> workQueue;

  private static long keepAliveTime;

  private static TimeUnit unit;

  static {
    // 读取配置文件或者环境变量，初始化各种参数

  }

  public AsyncServiceCallExecutor() {
    super(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
  }
}
