package com.qiuyj.qrpc.commons;

import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.commons.ClassUtils;
import com.qiuyj.commons.ReflectionUtils;
import com.qiuyj.qrpc.commons.annotation.RpcService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
public class DefaultServiceInstanceProvider implements ServiceInstanceProvider {

  private static final String PACKAGE_SUFFIX = "impl";

  private static final String CLASS_SUFFIX = "Impl";

  private final String packageSuffix;

  private final String classSuffix;

  public DefaultServiceInstanceProvider() {
    this(PACKAGE_SUFFIX, CLASS_SUFFIX);
  }

  public DefaultServiceInstanceProvider(String packageSuffix, String classSuffix) {
    if (Objects.isNull(packageSuffix)) {
      packageSuffix = ".";
    }
    else {
      if (!packageSuffix.startsWith(".")) {
        packageSuffix = "." + packageSuffix;
      }
      if (!packageSuffix.endsWith(".")) {
        packageSuffix += ".";
      }
    }
    this.packageSuffix = packageSuffix;
    this.classSuffix = Objects.isNull(classSuffix) ? "" : classSuffix;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getServiceInstance(Class<? super T> serviceInterface) {
    Objects.requireNonNull(serviceInterface, "serviceInterface == null.");
    RpcService anno = AnnotationUtils.findAnnotation(serviceInterface, RpcService.class);
    if (Objects.isNull(anno)) {
      throw new IllegalStateException("Illegal service interface, must annotated by @" + RpcService.class.getName());
    }
    else {
      Class<?> cls = anno.implementation();
      if (cls == Void.TYPE) {
        // 构建实例对象的class名称
        String instanceClassName = serviceInterface.getPackageName() + packageSuffix + serviceInterface.getSimpleName() + classSuffix;
        cls = ClassUtils.resolveClassName(instanceClassName, DefaultServiceInstanceProvider.class.getClassLoader());
      }
      // 判断是否是接口的子类
      if (!serviceInterface.isAssignableFrom(cls)) {
        throw new IllegalStateException("Service instance: " + cls + " is not an subclass of: " + serviceInterface);
      }
      else {
        // 得到无参构造函数
        Constructor<?> ctor = ReflectionUtils.getDefaultConstructorIfAvaliable(cls);
        Object instance;
        // 如果有默认的无参构造函数，那么就使用默认的构造函数创建对象
        if (Objects.nonNull(ctor)) {
          try {
            instance = ctor.newInstance();
          }
          catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create instance " + cls.getName() + " of " + serviceInterface + ".\nCaused by: " + e, e);
          }
        }
        else {
          // 没有默认的无参构造函数，那么只允许有一个有参构造函数，并且这些参数都是服务接口
          // 注意，这样无法解决循环引用，在构建的时候，千万要避免这种情况发生
          Constructor<?>[] ctors = cls.getDeclaredConstructors();
          if (ctors.length != 1) {
            throw new IllegalStateException("");
          }
          else {
            ctor = ctors[0];
            int paramLen = ctor.getParameterCount();
            Object[] params = new Object[paramLen];
            Class<?>[] paramTypes = ctor.getParameterTypes();
            for (int i = 0; i < paramLen; i++) {
              params[i] = getServiceInstance(paramTypes[i]);
            }
            try {
              instance = ctor.newInstance(params);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
              throw new IllegalStateException("Unable to create instance " + cls.getName() + " of " + serviceInterface + ".\nCaused by: " + e, e);
            }
          }
        }
        return (T) instance;
      }
    }
  }
}
