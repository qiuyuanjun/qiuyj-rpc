package com.qiuyj.qrpc.commons;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.commons.annotation.RpcService;

import java.util.function.Predicate;

/**
 * @author qiuyj
 * @since 2018-06-17
 */
public class RpcServicePredicate implements Predicate<Class<?>> {

  public static final RpcServicePredicate INSTANCE = new RpcServicePredicate();

  @Override
  public boolean test(Class<?> cls) {
    return cls.isInterface()
        && !cls.isAnnotation()
        && AnnotationUtils.hasAnnotation(cls, RpcService.class);
  }
}
