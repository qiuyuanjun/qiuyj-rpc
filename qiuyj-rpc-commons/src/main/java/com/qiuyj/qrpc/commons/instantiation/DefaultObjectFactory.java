package com.qiuyj.qrpc.commons.instantiation;

import com.qiuyj.commons.ReflectionUtils;

/**
 * @author qiuyj
 * @since 2018-09-03
 */
public class DefaultObjectFactory extends AbstractObjectFactory {

  @Override
  protected Object doNewInstance(Class<?> cls) {
    return ReflectionUtils.instantiateClass(cls);
  }
}
