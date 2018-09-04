package com.qiuyj.qrpc.server;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.commons.MethodSignUtils;
import com.qiuyj.qrpc.commons.ObjectMethods;
import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import com.qiuyj.qrpc.server.invoke.MethodInvoker;
import com.qiuyj.qrpc.server.invoke.ServiceProxy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiuyj
 * @since 2018-08-18
 */
public class ServiceExporter {

  private final Map<Class<?>, ServiceProxy> serviceProxyMap;

  public ServiceExporter(Collection<ClassInstanceValue<?>> serviceToExports) {
    serviceProxyMap = new HashMap<>();
    for (ClassInstanceValue<?> holder : serviceToExports) {
      Class<?> serviceInterface = holder.getServiceInterface();
      serviceProxyMap.put(serviceInterface, getServiceProxy(serviceInterface, holder.getInstance()));
    }
  }

  public ServiceProxy getServiceProxy(Class<?> serviceInterface) {
    return serviceProxyMap.get(serviceInterface);
  }

  private static ServiceProxy getServiceProxy(Class<?> serviceInterface, Object instance) {
    Map<String, MethodInvoker> invokers = new HashMap<>();
    for (Method method : serviceInterface.getMethods()) {
      // 排除从Object继承的方法
      if (ObjectMethods.INSTANCE.isObjectMethod(method)) {
        continue;
      }
      // rpc方法必须标注@RpcMethod注解
      if (!AnnotationUtils.hasAnnotation(method, RpcMethod.class)) {
        continue;
      }
      invokers.put(MethodSignUtils.getMethodSign(method), new MethodInvoker(method));
    }
    return new ServiceProxy(instance, invokers);
  }
}