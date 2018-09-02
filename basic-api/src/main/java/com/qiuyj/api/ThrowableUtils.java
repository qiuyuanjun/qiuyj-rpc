package com.qiuyj.api;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public abstract class ThrowableUtils {

  /**
   * 重新将{@code Throwable}异常分类抛出
   * @param t 异常
   */
  public static void rethrowException(Throwable t) {
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    else if (t instanceof Error) {
      throw (Error) t;
    }
    else {
      throw new UndeclaredThrowableException(t);
    }
  }
}
