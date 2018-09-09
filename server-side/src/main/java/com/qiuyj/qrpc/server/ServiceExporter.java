package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.commons.MethodSignUtils;
import com.qiuyj.qrpc.commons.ObjectMethods;
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

  /**
   * 当前rpc服务器所暴露的所有的服务 serviceInterface -> serviceProxy
   */
  private final Map<Class<?>, ServiceProxy> serviceProxyMap;

  public ServiceExporter(Collection<ClassInstanceValue<?>> serviceToExports) {
    serviceProxyMap = new HashMap<>();
    for (ClassInstanceValue<?> holder : serviceToExports) {
      Class<?> serviceInterface = holder.getServiceInterface();
      serviceProxyMap.put(serviceInterface, getServiceProxy(serviceInterface, holder.getInstance()));
    }
  }

  /**
   * 根据服务接口得到当前暴露的对应的rpc的{@code ServiceProxy}对象
   * @param serviceInterface 服务接口
   * @return {@code ServiceProxy}对象
   */
  public ServiceProxy getServiceProxy(Class<?> serviceInterface) {
    return serviceProxyMap.get(serviceInterface);
  }

  /**
   * 解析得到{@code ServiceProxy}对象
   * @param serviceInterface 服务接口
   * @param instance 服务实例
   * @return {@code ServiceProxy}对象
   */
  private static ServiceProxy getServiceProxy(Class<?> serviceInterface, Object instance) {
    Map<String, MethodInvoker> invokers = new HashMap<>();
    for (Method method : serviceInterface.getMethods()) {
      // 排除从Object继承的方法
      if (ObjectMethods.INSTANCE.isObjectMethod(method)) {
        continue;
      }
      // 2018-9-9修改，rpc方法无需强制标注@RpcMethod注解
      // rpc方法必须标注@RpcMethod注解
//      if (!AnnotationUtils.hasAnnotation(method, RpcMethod.class)) {
//        continue;
//      }
      invokers.put(MethodSignUtils.getMethodSign(method), new MethodInvoker(method));
    }
    return new ServiceProxy(instance, invokers);
  }
}