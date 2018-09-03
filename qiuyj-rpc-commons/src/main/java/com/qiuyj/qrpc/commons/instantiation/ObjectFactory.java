package com.qiuyj.qrpc.commons.instantiation;

/**
 * @author qiuyj
 * @since 2018-09-03
 */
public interface ObjectFactory {

  ObjectFactory INSTANCE = new DefaultObjectFactory();

  /**
   * 创建对象
   * @param cls 对象Class
   * @return 实例
   */
  Object newInstance(Class<?> cls);
}
