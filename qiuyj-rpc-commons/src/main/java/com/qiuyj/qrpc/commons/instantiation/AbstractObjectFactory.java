package com.qiuyj.qrpc.commons.instantiation;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-09-03
 */
public abstract class AbstractObjectFactory implements ObjectFactory {

  @Override
  public Object newInstance(Class<?> cls) {
    if (Objects.isNull(cls)) {
      throw new IllegalArgumentException("cls == null.");
    }
    else if (cls.isInterface() || cls.isEnum() || cls.isArray()) {
      throw new IllegalStateException("Object class only.");
    }
    else {
      return doNewInstance(cls);
    }
  }

  protected abstract Object doNewInstance(Class<?> cls);
}
