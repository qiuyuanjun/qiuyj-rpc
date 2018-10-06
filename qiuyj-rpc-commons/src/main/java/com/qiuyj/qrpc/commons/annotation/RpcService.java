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
   * 当前服务所属的应用名称，必填
   */
  String application();

  /**
   * 服务的版本号，可选
   */
  String version() default "1.0.0";

  /**
   * 服务的权重，1-10之间，值越大，将接收更多的请求
   */
  int weight() default 5;
}
