package com.qiuyj.qrpc.commons;

import com.qiuyj.commons.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-19
 */
public class ObjectMethods {

  /** 单例对象 */
  public static final ObjectMethods INSTANCE = new ObjectMethods();

  /** Object的方法列表 */
  private final List<ObjectMethod> objectMethods;

  private ObjectMethods() {
    objectMethods = new ArrayList<>(3);
    objectMethods.add(new Equals());
    objectMethods.add(new ToString());
    objectMethods.add(new HashCode());
  }

  /**
   * 判断当前的方法是否是{@code Object}申明的方法，或者从{@code Object}重载的方法
   * @param method 要判断的{@code Method}对象
   * @return 如果是，返回{@code true}，否则返回{@code false}
   */
  public boolean isObjectMethod(Method method) {
    Objects.requireNonNull(method, "method == null");
    if (method.getDeclaringClass() == Object.class) {
      return true;
    }
    String methodSign = MethodSignUtils.getMethodSign(method);
    return objectMethods.stream().anyMatch(objMethod -> objMethod.methodSign.equals(methodSign));
  }

  /**
   * 判断当前的方法是否是{@code Object}申明的方法，或者从{@code Object}重载的方法
   * @param methodName 方法名
   * @param args 方法的参数
   * @return 如果是，返回{@code true}，否则返回{@code false}
   */
  public boolean isObjectMethod(String methodName, Object... args) {
    Objects.requireNonNull(methodName, "methodName == null");
    String methodSign = MethodSignUtils.getMethodSign(methodName, args);
    return objectMethods.stream().anyMatch(objMethod -> objMethod.methodSign.equals(methodSign));
  }

  /**
   * 得到对应的{@code ObjectMethod}对象
   * @param methodName 方法名
   * @param args 方法参数
   * @return {@code ObjectMethod}对象
   */
  public ObjectMethod getObjectMethod(String methodName, Object... args) {
    if (StringUtils.isBlank(methodName)) {
      return null;
    }
    String methodSign = MethodSignUtils.getMethodSign(methodName, args);
    return objectMethods.stream()
        .filter(x$i -> x$i.methodSign.equals(methodSign))
        .findFirst()
        .orElse(null);
  }

  /**
   * 得到对应的{@code ObjectMethod}对象
   * @param methodSign 方法签名
   * @return {@code ObjectMethod}对象
   */
  public ObjectMethod getObjectMethod(String methodSign) {
    for (ObjectMethod objMethod : objectMethods) {
      if (objMethod.methodSign.equals(methodSign)) {
        return objMethod;
      }
    }
    return null;
  }

  public static abstract class ObjectMethod {

    /** 方法签名 */
    protected String methodSign;

    protected ObjectMethod(String methodSign) {
      this.methodSign = methodSign;
    }

    /**
     * 执行对应的方法
     * @param instance 对象实例
     * @param args 参数
     * @return 对应的方法返回值
     */
    public abstract Object invoke(Object instance, Object... args);
  }

  private static final class Equals extends ObjectMethod {

    protected Equals() {
      super("equals(Ljava/lang/Object;)");
    }

    @Override
    public Boolean invoke(Object instance, Object... args) {
      return instance.equals(args[0]);
    }
  }

  private static final class HashCode extends ObjectMethod {

    protected HashCode() {
      super("hashCode()");
    }

    @Override
    public Integer invoke(Object instance, Object... args) {
      return instance.hashCode();
    }
  }

  private static final class ToString extends ObjectMethod {

    protected ToString() {
      super("toString()");
    }

    @Override
    public String invoke(Object instance, Object... args) {
      return instance.toString();
    }
  }
}