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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRpcClient<T> extends AbstractClient implements ConfigurableRpcClient<T> {

  /**
   * 服务注册中心，所有的客户端共享一个服务注册中心
   */
  private static ServiceRegistry sharedServiceRegistry;
  static {
    sharedServiceRegistry = ServiceRegistryFactory.getServiceRegistry();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> sharedServiceRegistry.close()));
  }

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
    // TODO: 根据负载均衡算法选择一个服务连接
    ServiceInstance choosed = serviceInstances.get(0);
    remoteServerAddress = InetSocketAddress.createUnresolved(choosed.getIpAddress(), choosed.getPort());
    return Connection.EMPTY_CONNECTION;
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
