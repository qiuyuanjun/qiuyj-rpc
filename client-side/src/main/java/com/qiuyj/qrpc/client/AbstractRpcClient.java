package com.qiuyj.qrpc.client;

import com.qiuyj.api.Connection;
import com.qiuyj.api.client.AbstractClient;
import com.qiuyj.qrpc.client.proxy.ProxyFactory;
import com.qiuyj.qrpc.client.proxy.jdk.JdkProxyFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRpcClient<T> extends AbstractClient implements ConfigurableRpcClient<T> {

  /** 服务接口 */
  private Class<T> serviceInterface;

  /** 服务实例 */
  private T serviceInstance;

  /** 延迟初始化服务实例对象 */
  private boolean lazyInitServiceInstance;

  /** 远程服务器的地址 */
  private SocketAddress remoteServerAddress = new InetSocketAddress("127.0.0.1", 11221);

  private ProxyFactory proxyFactory = new JdkProxyFactory();

  protected AbstractRpcClient() {
    // for subclass
  }

  protected AbstractRpcClient(Class<T> serviceInterface) {
    setServiceInterface(serviceInterface);
  }

  @Override
  protected Connection doConnect() {
    // TODO 连接注册中心，获取对应服务器的ip地址和端口的list
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

  protected SocketAddress getRemoteServerAddress() {
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