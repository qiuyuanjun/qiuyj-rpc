package com.qiuyj.qrpc.server.invoke;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author qiuyj
 * @since 2018-08-19
 */
public class MethodInvoker {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodInvoker.class);

  private final Method method;

  /** 是否是异步执行 */
  private boolean asyncExecute;

  /** 异步执行的超时时间 */
  private int timeout;

  /** 时间单位 */
  private TimeUnit timeUnit;

  public MethodInvoker(Method method) {
    this.method = method;
    parseMethod(this);
  }

  public Object invoke(Object obj, Object... args) throws InvocationTargetException {
    try {
      return method.invoke(obj, args);
    } catch (IllegalAccessException e) {
      // ignore, never happen
    }
    throw new IllegalStateException("Never get here.");
  }

  /**
   * 解析当前的rpc方法
   * @param methodInvoker 当前对象
   */
  private static void parseMethod(MethodInvoker methodInvoker) {
    RpcMethod rpcMethod = AnnotationUtils.findAnnotation(methodInvoker.method, RpcMethod.class);
    if (Objects.nonNull(rpcMethod)) {
      methodInvoker.asyncExecute = rpcMethod.async();
      int timeout = rpcMethod.timeout();
      if (timeout > 0) {
        methodInvoker.timeout = timeout;
        methodInvoker.timeUnit = rpcMethod.timeUnit();
      }
    }
    if (LOGGER.isDebugEnabled()) {
      StringBuilder debugMsg = new StringBuilder("Method[")
          .append(methodInvoker.method.getName())
          .append(",asyncExecute=")
          .append(methodInvoker.asyncExecute);
      if (methodInvoker.hasTimeout()) {
        debugMsg.append(",timeout=")
            .append(methodInvoker.timeout)
            .append(",timeUnit=")
            .append(methodInvoker.timeUnit);
      }
      debugMsg.append("]");
      LOGGER.debug(debugMsg.toString());
    }
  }

  /**
   * 判断当前的方法是否是异步执行的方法，package可见
   */
  boolean isAsyncExecute() {
    return asyncExecute;
  }

  /**
   * 判断是否配置了超时时间
   * @return 如果配置了超时时间，那么返回{@code true}，否则返回{@code false}
   */
  boolean hasTimeout() {
    return timeout > 0;
  }

}
