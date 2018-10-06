package com.qiuyj.qrpc.server;

import com.qiuyj.api.server.AbstractServer;
import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.commons.StringUtils;
import com.qiuyj.commons.resource.ClassSeeker;
import com.qiuyj.qrpc.commons.annotation.RpcService;
import com.qiuyj.qrpc.commons.instantiation.ObjectFactory;
import com.qiuyj.qrpc.commons.instantiation.ServiceInstanceProvider;
import com.qiuyj.qrpc.registry.ServiceInstance;
import com.qiuyj.qrpc.registry.ServiceRegistry;
import com.qiuyj.qrpc.registry.ServiceRegistryFactory;
import com.qiuyj.qrpc.server.invoke.ServiceExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author qiuyj
 * @since 2018-06-12
 */
public abstract class AbstractRpcServer extends AbstractServer implements ConfigurableRpcServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcServer.class);

  /** rpc服务器的默认端口 */
  public static final int DEFAULT_PORT = 11221;

  /** 需要暴露的服务 Class -> ClassInstanceValue */
  private final Map<Class<?>, ClassInstanceValue<?>> serviceToExports = new ConcurrentHashMap<>();

  private volatile ServiceInstanceProvider serviceInstanceProvider;

  private volatile ObjectFactory objectFactory;

  /** 服务扫描路径 */
  private volatile String[] servicePackages;

  /** rpc服务器的端口，默认是11221 */
  private volatile int port = DEFAULT_PORT;

  /** 异步任务执行线程池 */
  private ExecutorService asyncExecutor;

  /** 服务注册中心 */
  private ServiceRegistry serviceRegistry;

  @Override
  protected void doStart() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Begin starting the rpc server.");
    }
    // 首先扫描所有的包，得到对应的Class对象
    Set<Class<?>> packageClasses = scanServicePackages(servicePackages);
    // 扫描到的Class对象可能有多种情况，可能是一个接口，也可能是一个服务接口的实现对象
    // 过滤掉那些有实现对象的服务接口
    ObjectFactory objectFactory = Objects.isNull(this.objectFactory) ? ObjectFactory.INSTANCE : this.objectFactory;
    Set<Class<?>> serviceInterfaces = filterImplementation(this, packageClasses, objectFactory);
    ServiceInstanceProvider provider = Objects.isNull(serviceInstanceProvider) ? ServiceInstanceProvider.DEFAULT : serviceInstanceProvider;
    // 将这些扫描到的Class临时保存到serviceToExports集合里面
    serviceToExports.putAll(resolveServiceInstance(serviceInterfaces, provider));
    // 暴露服务，将服务封装成ServiceProxy对象
    ServiceExporter serviceExporter = new ServiceExporter(serviceToExports.values());
    // 通过ServiceRegistryFactory获取具体对应的ServiceRegistry
    serviceRegistry = ServiceRegistryFactory.getServiceRegistry();
    // 将服务器注册到服务注册中心
    registerServices();
    // 启动服务器
    startInternal(serviceExporter);
  }

  /**
   * 将所有需要暴露的服务注册到服务注册中心
   */
  private void registerServices() {
    Map<Class<?>, ClassInstanceValue<?>> serviceInstanceMap = this.serviceToExports;
    serviceInstanceMap.forEach((key, value) -> {
      ServiceInstance serviceInstance = new ServiceInstance();
      serviceInstance.setIp(getLocalAddress().getHostAddress());
      serviceInstance.setPort(getPort());
      serviceInstance.setServiceName(key.getName());
      value.setRegistryInfo(serviceInstance);
      serviceRegistry.register(serviceInstance);
    });
  }

  /**
   * 服务器内部启动，交给具体的子类实现
   */
  protected abstract void startInternal(ServiceExporter serviceExporter);

  @Override
  protected void doClose() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Begin closing the rpc server.");
    }
    if (!serviceToExports.isEmpty()) {
      serviceToExports.clear();
    }
    if (Objects.nonNull(servicePackages)) {
      servicePackages = null;
    }
    closeInternal();
    // 关闭线程池
    asyncExecutor.shutdown();
  }

  /**
   * 服务器内部关闭，交给具体的子类实现
   */
  protected abstract void closeInternal();

  @Override
  public void setServiceInstanceProvider(ServiceInstanceProvider serviceInstanceProvider) {
    this.serviceInstanceProvider = serviceInstanceProvider;
  }

  @Override
  public void setObjectFactory(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  @Override
  public void setPort(int port) {
    if (port <= 1024) {
      LOGGER.warn("Port cannot be less than 1024. Use default port 11221 instead.");
      port = DEFAULT_PORT;
    }
    this.port = port;
  }

  protected ExecutorService getAsyncExecutor() {
    return asyncExecutor;
  }

  @Override
  public void setAsyncExecutor(ExecutorService asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public <T> void addServiceToExport(Class<? super T> serviceInterface, T instance) {
    if (!serviceInterface.isInterface()) {
      throw new IllegalArgumentException("Service to export must be an interface type.");
    }
    else if (Objects.isNull(instance)) {
      throw new IllegalArgumentException("instance == null.");
    }
    RpcService rpcServiceAnnotation = AnnotationUtils.findAnnotation(serviceInterface, RpcService.class);
    if (Objects.isNull(rpcServiceAnnotation)) {
      throw new IllegalStateException("Service interface must be annotated @RpcService annotation");
    }
    ClassInstanceValue<?> prev = serviceToExports.putIfAbsent(serviceInterface, new ClassInstanceValue<>(serviceInterface, instance, rpcServiceAnnotation));
    if (Objects.nonNull(prev)) {
      throw new ServiceExistException(prev);
    }
  }

  @Override
  public void setServicePackageToScan(String servicePackage, String... more) {
    Set<String> packages = new HashSet<>();
    if (Objects.nonNull(servicePackage)) {
      splitServicePackage(servicePackage, packages);
    }
    if (more.length > 0) {
      Arrays.stream(more)
          .filter(Objects::nonNull)
          .forEach(s -> splitServicePackage(s, packages));
    }
    this.servicePackages = packages.toArray(new String[0]);
  }

  private static void splitServicePackage(String s, Set<String> set) {
    String[] sArr = StringUtils.delimiteToStringArray(s, ConfigurableRpcServer.PACKAGE_SEPERATOR);
    set.addAll(Arrays.asList(sArr));
  }

  /**
   * 扫描所有指定的包含服务接口的包，生成对应的{@code Class}对象
   * @param servicePackages 包名数组
   * @return 扫描到的{{@code Class}集合
   */
  private static Set<Class<?>> scanServicePackages(String[] servicePackages) {
    Set<Class<?>> classSet = new HashSet<>();
    if (Objects.nonNull(servicePackages) && servicePackages.length > 0) {
      ClassSeeker classSeeker = new ClassSeeker(AbstractRpcServer.class.getClassLoader());
      classSeeker.setIfCondition(RpcServicePredicate.getInstance());
      for (String s : servicePackages) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Scanning package: " + s);
        }
        Class<?>[] clsArr = classSeeker.seekClasses(s);
        if (clsArr.length > 0) {
          classSet.addAll(Arrays.asList(clsArr));
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scaned classes " + Arrays.toString(clsArr));
          }
        }
      }
    }
    return classSet;
  }

  /**
   * 将所有扫描到的接口{@code Class}对象实例化，并且封装成{@code ClassInstanceValue}对象
   * @param packageClasses 所有扫描到的接口{@code Class}对象的{@code Set}集合
   * @param provider {@code ServiceInstanceProvider}
   * @return {@code ClassInstanceValue}的{@code Set}集合
   */
  private static Map<Class<?>, ClassInstanceValue<?>> resolveServiceInstance(Set<Class<?>> packageClasses, ServiceInstanceProvider provider) {
    Map<Class<?>, ClassInstanceValue<?>> tempValues = new HashMap<>();
    if (!packageClasses.isEmpty()) {
      for (Class<?> serviceInterface : packageClasses) {
        Object serviceInstance = provider.getServiceInstance(serviceInterface);
        if (Objects.nonNull(serviceInstance)) {
          // 同一个服务接口不能连续暴露服务两次
          if (tempValues.containsKey(serviceInterface)) {
            throw new ServiceExistException(serviceInterface);
          }
          tempValues.put(serviceInterface, ClassInstanceValue.newInstance(serviceInterface, serviceInstance));
        }
        else {
          throw new IllegalStateException("Service instance is null.");
        }
      }
    }
    return tempValues;
  }

  /**
   * 过滤掉一些扫描到的服务接口实现
   */
  private static Set<Class<?>> filterImplementation(AbstractRpcServer $this, Set<Class<?>> packageClasses, ObjectFactory objectFactory) {
    Set<Class<?>> interfaces = packageClasses.stream().filter(Class::isInterface).collect(Collectors.toSet());
    // 如果所有都是服务接口或者没有扫描到服务接口，那么直接返回
    if (interfaces.size() == 0 || interfaces.size() == packageClasses.size()) {
      return interfaces;
    }
    packageClasses.removeAll(interfaces);
    List<Class<?>> instantiations = new ArrayList<>();
    for (Class<?> cls : interfaces) {
      List<Object> instances = new ArrayList<>();
      for (Class<?> impl : packageClasses) {
        if (cls.isAssignableFrom(impl)) {
          instances.add(objectFactory.newInstance(impl));
        }
      }
      if (instances.size() > 1) {
        throw new IllegalStateException("Only one implement service instance are allowed.");
      }
      else if (instances.size() == 1) {
        $this.serviceToExports.put(cls, ClassInstanceValue.newInstance(cls, instances.get(0)));
        instantiations.add(cls);
      }
    }
    if (instantiations.size() > 0) {
      interfaces.removeAll(instantiations);
    }
    return interfaces;
  }

}
