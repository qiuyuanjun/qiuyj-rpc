package com.qiuyj.qrpc.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiuyj
 * @since 2018-06-12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RpcMethod {

  /**
   * 方法调用是否是异步调用，默认为同步调用
   */
  boolean async() default false;

  /**
   * 方法调用超时时间，单位为微秒ms
   * 如果为负数，那么表示永远不会超时
   */
  int timeout() default -1;
}
