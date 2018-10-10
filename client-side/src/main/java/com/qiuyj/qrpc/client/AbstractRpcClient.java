package com.qiuyj.qrpc.client;

import com.qiuyj.api.Connection;
import com.qiuyj.api.client.AbstractClient;
import com.qiuyj.commons.AnnotationUtils;
import com.qiuyj.qrpc.client.proxy.ProxyFactory;
import com.qiuyj.qrpc.client.proxy.jdk.JdkProxyFactory;
import com.qiuyj.qrpc.commons.annotation.RpcService;
import com.qiuyj.qrpc.registry.ServiceInstance;
import com.qiuyj.qrpc.registry.ServiceRegistry;
import com.qiuyj.qrpc.registry.ServiceRegistryFactory;
import com.qiuyj.qrpc.registry.SubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRpcClient<T> extends AbstractClient implements ConfigurableRpcClient<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcClient.class);

  /**
   * 服务注册中心，所有的客户端共享一个服务注册中心
   */
  private static final ServiceRegistry sharedServiceRegistry;

  /**
   * 当前进程所对应的所有注册的服务的本地缓存文件
   */
  private static final File SERVICE_INSTANCE_LOCAL_CACHE_FILE;

  static {
    sharedServiceRegistry = ServiceRegistryFactory.getServiceRegistry();
    Runtime.getRuntime().addShutdownHook(new Thread(sharedServiceRegistry::close));
    SERVICE_INSTANCE_LOCAL_CACHE_FILE = new File(buildTmpLocalCacheFilePath());
    File parentDir = SERVICE_INSTANCE_LOCAL_CACHE_FILE.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs();
    }
    try {
      SERVICE_INSTANCE_LOCAL_CACHE_FILE.createNewFile();
    }
    catch (IOException e) {
      throw new Error(e);
    }
    SERVICE_INSTANCE_LOCAL_CACHE_FILE.deleteOnExit();
  }

  /**
   * 构建缓存文件的文件名
   */
  static String buildTmpLocalCacheFilePath() {
    String userHome = System.getProperty("java.io.tmpdir");
    StringBuilder pathBuilder = new StringBuilder(64).append(userHome);
    if (!userHome.endsWith(File.separator)) {
      pathBuilder.append(File.separator);
    }
    pathBuilder.append("qrpc-cache")
        .append(File.separator)
        .append(ProcessHandle.current().pid())
        .append("$")
        .append("com.qiuyj.qrpc.registry.ServiceInstance.local");
    return pathBuilder.toString();
  }

  /**
   * 所有本地缓存的服务列表
   */
  private static Properties localCachedServiceInstances;

  /** 服务接口 */
  private Class<T> serviceInterface;

  /** 服务实例 */
  private T serviceInstance;

  /** 延迟初始化服务实例对象 */
  private boolean lazyInitServiceInstance;

  /** 远程服务器的地址 */
  private InetSocketAddress remoteServerAddress;

  /** 代理工厂 */
  private ProxyFactory proxyFactory = new JdkProxyFactory();

  protected AbstractRpcClient() {
    // for subclass
  }

  protected AbstractRpcClient(Class<T> serviceInterface) {
    setServiceInterface(serviceInterface);
  }

  @Override
  protected Connection doConnect() {
    List<ServiceInstance> serviceInstances = fetchFromServiceRegistry();
    // TODO: 根据负载均衡算法选择一个服务连接
    ServiceInstance choosed = serviceInstances.get(0);
    remoteServerAddress = InetSocketAddress.createUnresolved(choosed.getIpAddress(), choosed.getPort());
    return Connection.EMPTY_CONNECTION;
  }

  /**
   * 从注册中心抓取当前服务接口的所有部署的服务信息
   * @return 服务列表
   */
  private List<ServiceInstance> fetchFromServiceRegistry() {
    // 从服务注册中心得到当前服务的所有提供者
    RpcService rpcService = AnnotationUtils.findAnnotation(serviceInterface, RpcService.class);
    if (Objects.isNull(rpcService)) {
      throw new IllegalStateException("Service interface must be annotated by @RpcService annotation.");
    }
    // 构建订阅请求
    SubscribeRequest request = new SubscribeRequest();
    request.setApplicationName(rpcService.application());
    request.setVersion(rpcService.version());
    request.setName(serviceInterface.getName());
    // 从注册中心获取对应的ip地址和端口号列表
    List<ServiceInstance> serviceInstances = sharedServiceRegistry.subscribeServiceInstances(request);
    // 将结果存入到本地缓存文件里面
    try (FileWriter fw = new FileWriter(SERVICE_INSTANCE_LOCAL_CACHE_FILE, true)) {
      for (ServiceInstance serviceInstance : serviceInstances) {
        fw.write(buildLine(serviceInstance));
      }
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return serviceInstances;
  }

  private String buildLine(ServiceInstance serviceInstance) {
    return new StringBuilder(64)
        .append(serviceInstance.getApplicationName())
        .append(":")
        .append(serviceInstance.getName())
        .append(":")
        .append(serviceInstance.getVersion())
        .append("=")
        .append(serviceInstance.getIpAddress())
        .append(":")
        .append(serviceInstance.getPort())
        .append(System.lineSeparator())
        .toString();
  }

  @Override
  protected void doClose() {
    // 必须置为null
    // 如果没有置为null，那么一旦调用close()方法之后再次调用connect()方法
    // 那么可能会直接将这个不为null的对象返回
    serviceInstance = null;
  }

  @Override
  public void setServiceInterface(Class<T> serviceInterface) {
    Objects.requireNonNull(serviceInterface, "serviceInterface == null.");
    if (serviceInterface.isInterface() && !serviceInterface.isAnnotation()) {
      this.serviceInterface = serviceInterface;
    }
    else {
      throw new IllegalStateException("Must be an interface type.");
    }
  }

  @Override
  public Class<T> getServiceInterface() {
    return Objects.requireNonNull(serviceInterface);
  }

  @Override
  public T getServiceInstance() {
    if (lazyInitServiceInstance && Objects.isNull(serviceInstance)) {
      serviceInstance = (T) createServiceInstanceProxy(getConnection());
    }
    else if (Objects.isNull(serviceInstance)) {
      throw new IllegalStateException("serviceInstance == null. " +
          "You can call method setLazyInitServiceInstance() to enable lazy init the serviceInstance.");
    }
    return serviceInstance;
  }

  @Override
  protected void afterConnected(Connection connection) {
    // 读取本地服务缓存列表
    if (Objects.isNull(localCachedServiceInstances)) {
      localCachedServiceInstances = new Properties();
    }
    try (FileInputStream inputStream = new FileInputStream(SERVICE_INSTANCE_LOCAL_CACHE_FILE)) {
      localCachedServiceInstances.load(inputStream);
    }
    catch (IOException e) {
      LOGGER.warn("Error while loading local cached service instance file " + SERVICE_INSTANCE_LOCAL_CACHE_FILE.getPath(), e);
      localCachedServiceInstances = null;
    }

    if (lazyInitServiceInstance) {
      return;
    }
    if (Objects.isNull(serviceInterface)) {
      throw new IllegalStateException("Must set service interface before connect to the remote server." +
          "Please call method setServiceInterface() to set the serviceInterface variable.");
    }
    serviceInstance = (T) createServiceInstanceProxy(connection);
  }

  @Override
  public void setLazyInitServiceInstance() {
    lazyInitServiceInstance = true;
  }

  @Override
  public InetAddress getRemoteAddress() {
    return remoteServerAddress.getAddress();
  }

  /**
   * 得到与远程rpc服务器连接的{@code InetSocketAddress}对象，供子类使用
   */
  protected InetSocketAddress getRemoteServerAddress() {
    return remoteServerAddress;
  }

  /**
   * 创建服务接口的代理对象
   * @param connection 与远程服务器之间的连接
   * @return 接口实例对象
   */
  private Object createServiceInstanceProxy(Connection connection) {
    return proxyFactory.getProxy(getServiceInterface(), connection);
  }

}
