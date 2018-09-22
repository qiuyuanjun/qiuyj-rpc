package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.MethodSignUtils;
import com.qiuyj.qrpc.commons.ObjectMethods;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.server.invoke.MethodInvoker;
import com.qiuyj.qrpc.server.invoke.ServiceProxy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
   * 判断当前的请求是否是异步请求
   * @param serviceInterface 请求服务接口
   * @return 如果是，那么返回{@code true}，否则返回{@code false}
   */
  public boolean isAsyncRequest(Class<?> serviceInterface, RequestInfo requestInfo) {
    ServiceProxy serviceProxy = getServiceProxy(serviceInterface);
    if (Objects.isNull(serviceProxy)) {
      throw new RpcException(requestInfo.getRequestId(), ErrorReason.SERVICE_NOT_FOUND);
    }
    return serviceProxy.isAsyncRequest(requestInfo);
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