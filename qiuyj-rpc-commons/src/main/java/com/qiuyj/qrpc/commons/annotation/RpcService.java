package com.qiuyj.qrpc.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注一个接口为需要暴露的服务接口
 * @author qiuyj
 * @since 2018-06-12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcService {

  /**
   * 服务接口的实现类
   */
  Class<?> implementation() default void.class;

  /**
   * 当被标注了该注解的接口方法调用失败的时候，调用指定的类的对应的方法作为返回值
   */
  Class<?> fallback() default void.class;
}
