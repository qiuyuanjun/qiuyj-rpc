package com.qiuyj.qrpc.server.invoke;

import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.MethodSignUtils;
import com.qiuyj.qrpc.commons.ObjectMethods;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.commons.protocol.RequestInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-19
 */
public class ServiceProxy {

  private final Object serviceInstance;

  /** 同步调用的方法 */
  private Map<String, MethodInvoker> syncMethods;

  /** 异步调用的方法 */
  private Map<String, MethodInvoker> asyncMethods;

  public ServiceProxy(Object instance, Map<String, MethodInvoker> methods) {
    serviceInstance = instance;
    methods.forEach((methodSign, methodInvoker) -> {
      // 异步执行的方法
      if (methodInvoker.isAsyncExecute()) {
        if (Objects.isNull(asyncMethods)) {
          asyncMethods = new HashMap<>();
        }
        asyncMethods.put(methodSign, methodInvoker);
      }
      else {
        if (Objects.isNull(syncMethods)) {
          syncMethods = new HashMap<>();
        }
        syncMethods.put(methodSign, methodInvoker);
      }
    });

    if (Objects.nonNull(asyncMethods)) {
      asyncMethods = Collections.unmodifiableMap(asyncMethods);
    }

    if (Objects.nonNull(syncMethods)) {
      syncMethods = Collections.unmodifiableMap(syncMethods);
    }
  }

  /**
   * 执行rpc调用的方法
   * @param methodName 方法名称
   * @param args 方法参数
   * @return 方法执行结果
   */
  public Object call(String requestId, String methodName, Object... args) {
    String methodSign = MethodSignUtils.getMethodSign(methodName, args);
    // 1.判断当前执行的方法是否是Object的方法或者重载的Object的方法
    ObjectMethods.ObjectMethod objMethod = ObjectMethods.INSTANCE.getObjectMethod(methodSign);
    if (Objects.nonNull(objMethod)) {
      try {
        return objMethod.invoke(serviceInstance, args);
      }
      catch (Exception e) {
        throw new RpcException(requestId, ErrorReason.EXECUTE_SERVICE_ERROR);
      }
    }
    // 2.如果不是Object的方法，那么首先从syncMethods里面得到MethodInvoker
    MethodInvoker invoker = syncMethods.get(methodSign);
    if (Objects.nonNull(invoker)) {
      // 3.执行invoke调用方法执行，返回结果
      try {
        return invoker.invoke(serviceInstance, args);
      }
      catch (InvocationTargetException e) {
        throw new RpcException(requestId, ErrorReason.EXECUTE_SERVICE_ERROR);
      }
    }
    // 4.如果不是syncMethod，那么判断是否是asyncMethods
    else if (Objects.nonNull(invoker = asyncMethods.get(methodSign))) {
      // 5.异步执行被调用的服务方法

      // 直接返回null，后期将结果放入ResponseFuture
      return null;
    }
    else {
      // 没有对应的方法，直接抛出异常，交给ChannelHandler的exceptionCaught方法处理
      throw new RpcException(requestId, ErrorReason.SERVICE_NOT_FOUND);
    }
  }

  /**
   * 判断当前的请求是否是一个异步请求
   * @param requestInfo 请求体
   * @return 如果是，返回{@code true}，否则返回{@code false}
   */
  public boolean isAsyncRequest(RequestInfo requestInfo) {
    String methodSign = MethodSignUtils.getMethodSign(requestInfo.getMethodName(), requestInfo.getMethodParameters());
    return Objects.nonNull(asyncMethods) && asyncMethods.containsKey(methodSign);
  }
}