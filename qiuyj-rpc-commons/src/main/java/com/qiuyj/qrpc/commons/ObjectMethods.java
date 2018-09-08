package com.qiuyj.qrpc.commons;

import com.qiuyj.commons.StringUtils;

import java.lang.reflect.InvocationTargetException;
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

  /** 在当前的rpc环境下，Object的可以通过服务器端调用的方法列表 */
  private final List<ObjectMethod> canInvokeObjectMethods;

  /** 在当前rpc环境下，Object的不可以通过服务器端调用，并且客户端也不可以调用的方法列表 */
  private final List<ObjectMethod> canNotInvokeObjectMethods;

  private ObjectMethods() {
    canInvokeObjectMethods = new ArrayList<>(4);
    canInvokeObjectMethods.add(new Equals());
    canInvokeObjectMethods.add(new ToString());
    canInvokeObjectMethods.add(new HashCode());
    canInvokeObjectMethods.add(new Clone());

    canNotInvokeObjectMethods = new ArrayList<>(5);
    canNotInvokeObjectMethods.add(new Notify());
    canNotInvokeObjectMethods.add(new NotifyAll());
    canNotInvokeObjectMethods.add(new Wait());
    canNotInvokeObjectMethods.add(new WaitOfOneParameter());
    canNotInvokeObjectMethods.add(new WaitOfTwoParameter());
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
    return canInvokeObjectMethods.stream().anyMatch(objMethod -> objMethod.methodSign.equals(methodSign));
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
    return canInvokeObjectMethods.stream().anyMatch(objMethod -> objMethod.methodSign.equals(methodSign));
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
    return canInvokeObjectMethods.stream()
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
    for (ObjectMethod objMethod : canInvokeObjectMethods) {
      if (objMethod.methodSign.equals(methodSign)) {
        return objMethod;
      }
    }
    return null;
  }

  /**
   * 判断当前方法是否是不可执行的Object的方法
   * @param method 方法
   * @return 如果是，返回{@code true}，否则返回{@code false}
   */
  public boolean isNotExecutableObjectMethod(Method method) {
    Objects.requireNonNull(method, "method == null");
    if (method.getDeclaringClass() != Object.class) {
      return false;
    }
    String methodSign = MethodSignUtils.getMethodSign(method);
    return canNotInvokeObjectMethods.stream().anyMatch(x$i -> x$i.methodSign.equals(methodSign));
  }

  /**
   * 判断当前方法是否是不可执行的Object的方法
   * @param methodSign 方法签名
   * @return 如果是，返回{@code true}，否则返回{@code false}
   */
  public boolean isNotExecutableObjectMethod(String methodSign) {
    for (ObjectMethod objMethod : canNotInvokeObjectMethods) {
      if (objMethod.methodSign.equals(methodSign)) {
        return true;
      }
    }
    return false;
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
    public abstract Object invoke(Object instance, Object... args) throws Exception;
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

  private static final class Clone extends ObjectMethod {

    protected Clone() {
      super("clone()");
    }

    @Override
    public Object invoke(Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
      // 由于clone方法比较特殊，这里需要通过反射调用
      Method cloneMethod = null;
      try {
        cloneMethod = instance.getClass().getDeclaredMethod("clone");
      } catch (NoSuchMethodException e) {
        // ignore
        // 如果能够调用clone方法，那么表明rpc接口一定重新声明了clone方法
        // 所以这里这个异常可以直接忽略
      }
      if (Objects.nonNull(cloneMethod)) {
        return cloneMethod.invoke(instance, args);
      }
      throw new IllegalStateException("Never get here.");
    }
  }

  private static abstract class NonInvokeObjectMethod extends ObjectMethod {

    protected NonInvokeObjectMethod(String methodSign) {
      super(methodSign);
    }

    @Override
    public String invoke(Object instance, Object... args) {
      throw new UnsupportedOperationException("Method " + methodSign + " that in rpc environment is not executable.");
    }
  }

  private static final class Notify extends NonInvokeObjectMethod {

    protected Notify() {
      super("notify()");
    }
  }

  private static final class NotifyAll extends NonInvokeObjectMethod {

    protected NotifyAll() {
      super("notifyAll()");
    }
  }

  private static final class Wait extends NonInvokeObjectMethod {

    protected Wait() {
      super("wait()");
    }
  }

  private static final class WaitOfOneParameter extends NonInvokeObjectMethod {

    protected WaitOfOneParameter() {
      super("wait(J)");
    }
  }

  private static final class WaitOfTwoParameter extends NonInvokeObjectMethod {

    protected WaitOfTwoParameter() {
      super("wait(JI)");
    }
  }
}
