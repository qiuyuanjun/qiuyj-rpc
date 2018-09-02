package com.qiuyj.qrpc.server;

import com.qiuyj.api.server.AbstractServer;
import com.qiuyj.commons.StringUtils;
import com.qiuyj.commons.resource.ClassSeeker;
import com.qiuyj.qrpc.commons.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiuyj
 * @since 2018-06-12
 */
public abstract class AbstractRpcServer extends AbstractServer implements ConfigurableRpcServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcServer.class);

  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** rpc服务器的默认端口 */
  public static final int DEFAULT_PORT = 11221;

  /** 需要暴露的服务 Class -> ClassInstanceValue */
  private final Map<Class<?>, ClassInstanceValue<?>> serviceToExports = new ConcurrentHashMap<>();

  private volatile ServiceInstanceProvider serviceInstanceProvider;

  /** 服务扫描路径 */
  private volatile String[] servicePackages;

  /** rpc服务器的端口，默认是11221 */
  private volatile int port = DEFAULT_PORT;

  @Override
  protected void doStart() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Begin starting the rpc server.");
    }
    // 首先扫描所有的包，得到对应的Class对象
    Set<Class<?>> packageClasses = scanServicePackages(servicePackages);
    ServiceInstanceProvider provider = Objects.isNull(serviceInstanceProvider) ? ServiceInstanceProvider.DEFAULT : serviceInstanceProvider;
    // 将这些扫描到的Class临时保存到serviceToExports集合里面
    serviceToExports.putAll(resolveServiceInstance(packageClasses, provider));
    // 暴露服务，将服务封装成ServiceProxy对象
    ServiceExporter serviceExporter = new ServiceExporter(serviceToExports.values());
    // TODO 将服务器注册到服务注册中心

    // 启动服务器
    startInternal(serviceExporter);
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
  }

  /**
   * 服务器内部关闭，交给具体的子类实现
   */
  protected abstract void closeInternal();

  @Override
  public RpcContext getContext() {
    return null;
  }

  @Override
  public void setServiceInstanceProvider(ServiceInstanceProvider serviceInstanceProvider) {
    this.serviceInstanceProvider = serviceInstanceProvider;
  }

  @Override
  public void setPort(int port) {
    if (port <= 1024) {
      LOGGER.warn("Port cannot be less than 1024. Use default port 11221 instead.");
      port = DEFAULT_PORT;
    }
    this.port = port;
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
    ClassInstanceValue<?> prev = serviceToExports.putIfAbsent(serviceInterface, new ClassInstanceValue<>(serviceInterface, instance));
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
      classSeeker.setIfCondition(RpcServicePredicate.INSTANCE);
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

}