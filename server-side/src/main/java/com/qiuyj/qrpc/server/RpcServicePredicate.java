package com.qiuyj.qrpc.server;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.commons.annotation.RpcService;
import com.qiuyj.qrpc.server.annotation.RpcServiceImpl;

import java.util.function.Predicate;

/**
 * @author qiuyj
 * @since 2018-06-17
 */
public class RpcServicePredicate implements Predicate<Class<?>> {

  public static final RpcServicePredicate INSTANCE = new RpcServicePredicate();

  @Override
  public boolean test(Class<?> cls) {
    // 如果当前被扫描的Class对象是一个接口
    // 那么其一定不能是注解并且一定要被@RpcService注解标注
    // 如果当前被扫描的Class对象不是一个接口类型
    // 那么其一定不能是枚举类型，并且一定不能是java虚拟机生成的类（各种内部类），并且一定要被@RpcServiceImpl注解标注
    return cls.isInterface() ?
        !cls.isAnnotation() && AnnotationUtils.hasAnnotation(cls, RpcService.class) :
        !cls.isEnum() && !cls.isSynthetic() && AnnotationUtils.hasAnnotation(cls, RpcServiceImpl.class);
  }
}
