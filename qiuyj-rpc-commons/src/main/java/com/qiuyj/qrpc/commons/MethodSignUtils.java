package com.qiuyj.qrpc.commons;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-19
 */
public abstract class MethodSignUtils {

  /**
   * 得到方法签名，类似jvm的内部方法表示
   * @param methodName 方法名
   * @param args 参数
   * @return 对应的方法签名
   */
  public static String getMethodSign(String methodName, Object... args) {
    if (Objects.nonNull(args)) {
      Class<?>[] argTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i].getClass();
      }
      return getMethodSign(methodName, argTypes);
    }
    return getMethodSign(methodName, (Class<?>[]) null);
  }

  private static void replaceBoxingTypeToPrimitiveType(Class<?>[] argTypes, int idx) {
    Class<?> type = argTypes[idx];
    if (type.isPrimitive() || type.isArray()) {
      return;
    }
    if (type == Integer.class) {
      argTypes[idx] = Integer.TYPE;
    }
    else if (type == Boolean.class) {
      argTypes[idx] = Boolean.TYPE;
    }
    else if (type == Character.class) {
      argTypes[idx] = Character.TYPE;
    }
    else if (type == Long.class) {
      argTypes[idx] = Long.TYPE;
    }
    else if (type == Short.class) {
      argTypes[idx] = Short.TYPE;
    }
    else if (type == Byte.class) {
      argTypes[idx] = Byte.TYPE;
    }
    else if (type == Double.class) {
      argTypes[idx] = Double.TYPE;
    }
    else if (type == Float.class) {
      argTypes[idx] = Float.TYPE;
    }
  }

  public static String getMethodSign(String methodName, Class<?>... args) {
    Objects.requireNonNull(methodName, "methodName == null");
    StringBuilder methodSignBuilder = new StringBuilder(methodName);
    methodSignBuilder.append("(");
    if (Objects.nonNull(args)) {
      StringBuilder joiner = new StringBuilder();
      for (int i = 0; i < args.length; i++) {
        replaceBoxingTypeToPrimitiveType(args, i);
        joiner.append(getTypeDescriptor(args[i]));
      }
      methodSignBuilder.append(joiner);
    }
    methodSignBuilder.append(")");
    return methodSignBuilder.toString();
  }

  public static String getMethodSign(Method method) {
    return getMethodSign(method.getName(), method.getParameterTypes());
  }

  private static String getTypeDescriptor(Class<?> type) {
    if (type.isPrimitive()) {
      if (type == Boolean.TYPE) {
        return "Z";
      }
      else if (type == Byte.TYPE) {
        return "B";
      }
      else if (type == Character.TYPE) {
        return "C";
      }
      else if (type == Double.TYPE) {
        return "D";
      }
      else if (type == Float.TYPE) {
        return "F";
      }
      else if (type == Integer.TYPE) {
        return "I";
      }
      else if (type == Long.TYPE) {
        return "J";
      }
      else if (type == Short.TYPE) {
        return "S";
      }
      else {
        throw new IllegalStateException("Never get here.");
      }
    }
    else if (type.isArray()) {
      return "[" + getTypeDescriptor(type.getComponentType());
    }
    else {
      return "L" + type.getName().replace(".", "/") + ";";
    }
  }

}