package com.qiuyj.qrpc.server.invoke;

import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.MethodSignUtils;
import com.qiuyj.qrpc.commons.ObjectMethods;
import com.qiuyj.qrpc.commons.RpcException;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-19
 */
public class ServiceProxy {

  private final Object serviceInstance;

  private final Map<String, MethodInvoker> methods;

  public ServiceProxy(Object instance, Map<String, MethodInvoker> methods) {
    serviceInstance = instance;
    this.methods = Collections.unmodifiableMap(methods);
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
      return objMethod.invoke(serviceInstance, args);
    }
    // 2.如果不是Object的方法，那么得到对应的MethodInvoker对象
    MethodInvoker invoker = methods.get(methodSign);
    if (Objects.isNull(invoker)) {
      // 没有对应的方法，直接抛出异常，交给ChannelHandler的exceptionCaught方法处理
      throw new RpcException(requestId, ErrorReason.SERVICE_NOT_FOUND);
    }
    // 3.执行invoke调用方法执行，返回结果
    try {
      return invoker.invoke(serviceInstance, args);
    }
    catch (InvocationTargetException e) {
      throw new RpcException(requestId, ErrorReason.EXECUTE_SERVICE_ERROR);
    }
  }
}