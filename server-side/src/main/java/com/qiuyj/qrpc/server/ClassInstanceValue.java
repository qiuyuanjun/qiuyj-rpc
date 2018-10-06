package com.qiuyj.qrpc.server;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.commons.annotation.RpcService;
import com.qiuyj.qrpc.registry.ServiceInstance;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-06-13
 */
@SuppressWarnings("unchecked")
public class ClassInstanceValue<T> {

  private Class<? super T> serviceInterface;

  private T instance;

  /** 当前服务接口所注解的@RpcService注解实例 */
  private RpcService rpcService;

  private ClassInstanceValue() {
    // for inner usages
  }

  public ClassInstanceValue(Class<? super T> serviceInterface, T instance, RpcService rpcService) {
    this.serviceInterface = serviceInterface;
    this.instance = instance;
    this.rpcService = rpcService;
  }

  public Class<? super T> getServiceInterface() {
    return serviceInterface;
  }

  public void setServiceInterface(Class<?> serviceInterface) {
    this.serviceInterface = (Class<? super T>) serviceInterface;
  }

  public T getInstance() {
    return instance;
  }

  public void setInstance(Object instance) {
    this.instance = (T) instance;
  }

  public void setRpcService(RpcService rpcService) {
    this.rpcService = rpcService;
  }

  /**
   * 设置服务注册相关的一些公共信息，比如应用名称，服务接口版本号，权重等等
   * @param serviceInstance {@code ServiceInstance}对象
   */
  void setRegistryInfo(ServiceInstance serviceInstance) {
    // version
    serviceInstance.setVersion(rpcService.version());
    // applicationName
    serviceInstance.setApplicationName(rpcService.application());
    // weight
    serviceInstance.setWeight(rpcService.weight());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassInstanceValue<?> that = (ClassInstanceValue<?>) o;
    return Objects.equals(serviceInterface, that.serviceInterface) &&
        Objects.equals(instance, that.instance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceInterface, instance);
  }

  /**
   * 提供给外部方便的创建对象的方法，主要是考虑到了没有泛型的情况
   * @param serviceInterface 接口
   * @param instance 接口实例
   * @return {@code ClassInstanceValue}对象
   */
  public static ClassInstanceValue<?> newInstance(Class<?> serviceInterface, Object instance) {
    if (!serviceInterface.isInterface()) {
      throw new IllegalArgumentException("ServiceInterface must be an interface.");
    }
    else if (Objects.isNull(instance)) {
      throw new IllegalArgumentException("Instance == null.");
    }
    else if (serviceInterface.isInstance(instance)) {
      RpcService rpcService = AnnotationUtils.findAnnotation(serviceInterface, RpcService.class);
      if (Objects.isNull(rpcService)) {
        throw new IllegalStateException("Service interface must be annotated @RpcService annotation");
      }
      ClassInstanceValue<?> result = new ClassInstanceValue<>();
      result.setServiceInterface(serviceInterface);
      result.setInstance(instance);
      result.setRpcService(rpcService);
      return result;
    }
    else {
      throw new IllegalArgumentException("Instance: " + instance + " is not an subclass of: " + serviceInterface);
    }
  }
}
