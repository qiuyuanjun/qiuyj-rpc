package com.qiuyj.qrpc.commons;

import com.qiuyj.commons.ReflectionUtils;
import org.objectweb.asm.*;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于asm提供的字节码生成，提供比原生java反射更快的方法调用方案
 * @author qiuyj
 * @since 2018-09-24
 */
public abstract class FastMethodInvoker {

  /** {@code FastMethodInvoker}缓存 */
  private static final Map<Class<?>, FastMethodInvoker> FAST_METHOD_INVOKER_CACHE
      = new HashMap<>();

  /** 方法列表 */
  private final String[] methodNames;

  /** 方法的参数列表 */
  private final Class<?>[][] parameterTypes;

  /**
   * 私有构造函数，通过工厂方法实例化
   * @param methodNames 方法列表
   */
  protected FastMethodInvoker(String[] methodNames, Class<?>[][] parameterTypes) {
    this.methodNames = methodNames;
    this.parameterTypes = parameterTypes;
  }

  /**
   * 执行对应的方法，此方法通过ASM框架动态生成
   * @param instance 方法所属的对象
   * @param methodIndex 方法下标
   * @param args 方法参数
   * @return 方法执行结果
   */
  public abstract Object invoke(Object instance, int methodIndex, Object... args);

  /**
   * 得到对应的方法所属的下标
   * @param methodName 方法名
   * @param argsTypes 方法参数列表
   * @return 对应的下标
   */
  public int getMethodIndex(String methodName, Class<?>... argsTypes) {
    int argsLen = Objects.isNull(argsTypes) ? 0 : argsTypes.length;
    for (int i = 0; i < methodNames.length; i++) {
      if (methodNames[i].equals(methodName)) {
        Class<?>[] parameterTypes = this.parameterTypes[i];
        if (parameterTypes.length == argsLen) {
          return i;
        }
        else {
          boolean equals = true;
          Class<?> type;
          for (int j = 0; j < parameterTypes.length; j++) {
            type = parameterTypes[j];
            if (argsTypes[j] != type || !type.isAssignableFrom(argsTypes[j])) {
              equals = false;
              break;
            }
          }
          if (equals) {
            return i;
          }
        }
      }
    }
    throw new IllegalStateException("Can not find method index of method: " + methodName);
  }

  /**
   * 得到{@code Class<?>}对应的{@code FastMethodInvoker}对象
   * @param cls {@code Class<?>}对象
   * @return 对应的{{@code FastMethodInvoker}对象
   */
  public static FastMethodInvoker getInstance(Class<?> cls) {
    Objects.requireNonNull(cls, "cls == null");
    FastMethodInvoker instance = FAST_METHOD_INVOKER_CACHE.get(cls);
    if (Objects.isNull(instance)) {
      // 这里使用cls作为锁的原因是可以减少锁的粒度，增加并发访问
      // 如果这里采用FAST_METHOD_INVOKER_CACHE作为锁的话
      // 那么在并发很高的情况下，会有很多线程被阻塞在这一步
      synchronized (getClassLock(cls)) {
        instance = FAST_METHOD_INVOKER_CACHE.get(cls);
        if (Objects.isNull(instance)) {
          // 通过asm字节码框架动态生成FastMethodInvoker的实现类
          instance = create(cls);
          FAST_METHOD_INVOKER_CACHE.putIfAbsent(cls, instance);
        }
      }
    }
    return instance;
  }

  /**
   * 创建FastMethodInvoker的实现类，通过asm
   * @param cls {@code Class}对象
   * @return {@code FastMethodInvoker}的实现类
   */
  private static FastMethodInvoker create(Class<?> cls) {
    Method[] methods = getMethods(cls);
    String[] methodNames = new String[methods.length];
    Class<?>[][] methodParameters = new Class<?>[methods.length][];
    Class<?>[] returnTypes = new Class<?>[methods.length];
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      methodNames[i] = method.getName();
      methodParameters[i] = method.getParameterTypes();
      returnTypes[i] = method.getReturnType();
    }

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    String superClassName = Type.getInternalName(FastMethodInvoker.class);
    String wrappedClassName = Type.getInternalName(cls);
    String generateClassName = wrappedClassName + "$EnhancedByQiuyj";
    cw.visit(Opcodes.V10,
        Opcodes.ACC_PRIVATE + Opcodes.ACC_SUPER,
        generateClassName,
        null,
        superClassName,
        null);
    // 构造函数
    {
      String constructorName = "<init>",
             constructorDesc = "([Ljava/lang/String;[[Ljava/lang/Class;)V";
      MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PROTECTED,
          constructorName,
          constructorDesc,
          null,
          null);
      mv.visitCode();
      mv.visitVarInsn(Opcodes.ALOAD, 0); // aload_0
      mv.visitVarInsn(Opcodes.ALOAD, 1); // aload_1
      mv.visitVarInsn(Opcodes.ALOAD, 2); // aload_2
      // invokespecial #1
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
          superClassName,
          constructorName,
          constructorDesc,
          false);
      mv.visitInsn(Opcodes.RETURN); // return
      mv.visitMaxs(3, 3);
      mv.visitEnd();
    }
    // invoke(Object instance, int methodIndex, Object... args)函数
    {
      String invokeMethodName = "invoke",
             invokeMethodDesc = "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;";
      MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_VARARGS,
          invokeMethodName,
          invokeMethodDesc,
          null,
          null);
      mv.visitCode();
      int methodLen = methodNames.length;
      if (methodLen > 0) {
        // 生成switch语句
        mv.visitVarInsn(Opcodes.ILOAD, 2); // iload_2
        Label[] labels = new Label[methodLen];
        for (int i = 0; i < methodLen; i++) {
          labels[i] = new Label();
        }
        Label defaultLabel = new Label();
        /*
        1: tableswitch   { // 0 to 2

                       0: 28

                       1: 52

                       2: 66
                 default: 100
            }
         */
        mv.visitTableSwitchInsn(0, methodLen - 1, defaultLabel, labels);
        StringBuilder buf = new StringBuilder(128);
        for (int i = 0; i < methodLen; i++) {
          mv.visitLabel(labels[i]);
          // StackMapTable: number_of_entries = 4
          // frame_type = 28 /* same */
          // frame_type = 23 /* same */
          // frame_type = 13 /* same */
          // frame_type = 33 /* same */
          mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
          mv.visitVarInsn(Opcodes.ALOAD, 1); // aload_1
          // 将invoke方法的第一个参数强转为被包装的类
          mv.visitTypeInsn(Opcodes.CHECKCAST, wrappedClassName); // checkcast #2
          mv.visitVarInsn(Opcodes.ASTORE, 4); // astore_4
          // 这样做，可以重复使用StringBuilder，不必每次生成一个方法，就要重新new一个StringBuilder
          buf.setLength(0);
          buf.append("(");
          Class<?>[] paramTypes = methodParameters[i];
          int methodParamCount = paramTypes.length;
          int paramIndex = 5, // astore下标
              constIndex = 3; // iconst_0对应的值，每循环一次加一（即iconst_1,iconst_2等等）
          for (Class<?> paramType : methodParameters[i]) {
            mv.visitVarInsn(Opcodes.ALOAD, 3); // aload_3
            mv.visitInsn(constIndex++); // iconst_0 (iconst_1...)
            mv.visitInsn(Opcodes.AALOAD); // aaload
            // 对参数进行类型转换
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
            buf.append(Type.getDescriptor(paramType));
            mv.visitVarInsn(Opcodes.ASTORE, paramIndex++);
          }
          buf.append(")");
          mv.visitVarInsn(Opcodes.ALOAD, 4); // aload_4
          for (int j = 0; j < methodParamCount; j++) {
            mv.visitVarInsn(Opcodes.ALOAD, paramIndex - methodParamCount + j);
          }
          Class<?> returnType = returnTypes[i];
          buf.append(Type.getDescriptor(returnType));
          // 执行方法
          if (cls.isInterface()) {
            // 接口方法
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, wrappedClassName, methodNames[i], buf.toString(), true);
          }
          else if (Modifier.isStatic(cls.getModifiers())) {
            // 静态方法
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrappedClassName, methodNames[i], buf.toString(), false);
          }
          else {
            // 类方法
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrappedClassName, methodNames[i], buf.toString(), false);
          }
          // 对基本数据类型的返回值做做包装
          if (returnType == Integer.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
          }
          else if (returnType == Boolean.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
          }
          else if (returnType == Byte.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
          }
          else if (returnType == Long.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
          }
          else if (returnType == Float.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
          }
          else if (returnType == Double.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
          }
          else if (returnType == Short.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
          }
          else if (returnType == Character.TYPE) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
          }
          else if (returnType == Void.TYPE) {
            mv.visitInsn(Opcodes.ACONST_NULL);
          }
          mv.visitInsn(Opcodes.ARETURN);
        }
        mv.visitLabel(defaultLabel);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      }
      // 生成异常抛出语句 throw new IllegalStateException("Method not found.");
      mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
      mv.visitInsn(Opcodes.DUP);
      mv.visitLdcInsn("Method not found.");
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V", false);
      mv.visitInsn(Opcodes.ATHROW);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }
    cw.visitEnd();
    // 得到定义的类的字节数组
    byte[] bytes = cw.toByteArray();
    AccessorClassLoader classLoader = AccessorClassLoader.get(cls);
    Class<?> generatedClass = classLoader.defineClass(cls.getName() + "$EnhancedByQiuyj", bytes);
    // 得到构造函数
    Constructor<?> ctor = ReflectionUtils.getConstructor(generatedClass, methodNames.getClass(), methodParameters.getClass());
    ctor.setAccessible(true);
    try {
      return (FastMethodInvoker) ctor.newInstance(methodNames, methodParameters);
    }
    catch (Exception e) {
      throw new InternalError(e);
    }
  }

  /**
   * 得到Class的锁
   */
  private static Class<?> getClassLock(Class<?> lock) {
    return lock;
  }

  /**
   * 得到当前类所能访问到的所有方法的数组
   * @param cls 类对象
   * @return {@code Method}数组
   */
  private static Method[] getMethods(Class<?> cls) {
    Method[] methods;
    if (cls.isInterface()) {
      methods = cls.getMethods();
    }
    else {
      Set<Method> methodSet = new HashSet<>();
      while (Objects.nonNull(cls) && cls != Object.class) {
        methodSet.addAll(Arrays.stream(cls.getDeclaredMethods()).filter(m -> {
          int methodModifiers = m.getModifiers();
          return !Modifier.isPrivate(methodModifiers) &&
              !Modifier.isProtected(methodModifiers) &&
              !Modifier.isAbstract(methodModifiers);
        }).collect(Collectors.toSet()));
        cls = cls.getSuperclass();
      }
      methods = methodSet.toArray(new Method[0]);
    }
    return methods;
  }

  // forked from https://github.com/fengjiachun/Jupiter/blob/master/jupiter-common/src/main/java/org/jupiter/common/util/FastMethodAccessor.java
  static class AccessorClassLoader extends ClassLoader {

    private static final WeakHashMap<ClassLoader, WeakReference<AccessorClassLoader>> accessorClassLoaders = new WeakHashMap<>();

    private static final ClassLoader selfContextParentClassLoader = getParentClassLoader(AccessorClassLoader.class);
    private static volatile AccessorClassLoader selfContextAccessorClassLoader = new AccessorClassLoader(selfContextParentClassLoader);

    public AccessorClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class<?> defineClass(String name, byte[] bytes) throws ClassFormatError {
      return defineClass(name, bytes, 0, bytes.length, getClass().getProtectionDomain());
    }

    static AccessorClassLoader get(Class<?> type) {
      ClassLoader parent = getParentClassLoader(type);

      // 1. 最快路径:
      if (selfContextParentClassLoader.equals(parent)) {
        if (selfContextAccessorClassLoader == null) {
          synchronized (accessorClassLoaders) { // DCL with volatile semantics
            if (selfContextAccessorClassLoader == null)
              selfContextAccessorClassLoader = new AccessorClassLoader(selfContextParentClassLoader);
          }
        }
        return selfContextAccessorClassLoader;
      }

      // 2. 常规查找:
      synchronized (accessorClassLoaders) {
        WeakReference<AccessorClassLoader> ref = accessorClassLoaders.get(parent);
        if (ref != null) {
          AccessorClassLoader accessorClassLoader = ref.get();
          if (accessorClassLoader != null) {
            return accessorClassLoader;
          } else {
            accessorClassLoaders.remove(parent); // the value has been GC-reclaimed, but still not the key (defensive sanity)
          }
        }
        AccessorClassLoader accessorClassLoader = new AccessorClassLoader(parent);
        accessorClassLoaders.put(parent, new WeakReference<>(accessorClassLoader));
        return accessorClassLoader;
      }
    }

    private static ClassLoader getParentClassLoader(Class<?> type) {
      ClassLoader parent = type.getClassLoader();
      if (parent == null) {
        parent = ClassLoader.getSystemClassLoader();
      }
      return parent;
    }
  }
}
