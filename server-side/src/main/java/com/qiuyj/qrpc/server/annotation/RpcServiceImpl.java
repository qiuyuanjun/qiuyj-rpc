package com.qiuyj.qrpc.server.annotation;

import com.qiuyj.qrpc.server.AbstractRpcServer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务接口实现类注解，该注解用于{@link AbstractRpcServer#scanServicePackages(String[])}方法
 * @author qiuyj
 * @since 2018-09-03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcServiceImpl {

  /**
   * 当接口方法调用失败的时候，就会执行该项目指定的代替类里面对应的方法
   */
  Class<?> fallback() default void.class;
}