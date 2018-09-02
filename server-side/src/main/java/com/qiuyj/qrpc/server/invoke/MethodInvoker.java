package com.qiuyj.qrpc.server.invoke;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author qiuyj
 * @since 2018-08-19
 */
public class MethodInvoker {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodInvoker.class);

  private final Method method;

  /** 是否是异步执行 */
  private boolean asyncExecute;

  public MethodInvoker(Method method) {
    this.method = method;
    parseMethod(this);
  }

  public Object invoke(Object obj, Object... args) {
    try {
      return method.invoke(obj, args);
    } catch (IllegalAccessException e) {
      // ignore, never happen
    } catch (InvocationTargetException e) {
      LOGGER.error("Error while executing method: " + method.getName() + ".\nCaused by: " + e, e);
      throw new RpcException();
    }
    throw new IllegalStateException("Never get here.");
  }

  private static void parseMethod(MethodInvoker methodInvoker) {
    RpcMethod rpcMethod = AnnotationUtils.findAnnotation(methodInvoker.method, RpcMethod.class);
    methodInvoker.asyncExecute = rpcMethod.async();
  }
}