package com.qiuyj.qrpc.registry.zookeeper;

import com.qiuyj.qrpc.registry.AbstractServiceRegistry;
import com.qiuyj.qrpc.registry.ServiceInstance;
import com.qiuyj.qrpc.registry.ServiceRegistryException;
import com.qiuyj.qrpc.registry.SubscribeRequest;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper注册中心
 * @author qiuyj
 * @since 2018-10-04
 */
public class ZookeeperServiceRegistry extends AbstractServiceRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

  /** 空数据 */
  private static final byte[] EMPTY_DATA = new byte[0];

  /** curator客户端 */
  private CuratorFramework zkClient;

  @Override
  protected void checkConnectionState(boolean necessary) {
    if (necessary && !zkClient.getZookeeperClient().isConnected()) {
      // 此时，在60s没并没有连上zookeeper服务器，那么直接抛出异常
      throw new ServiceRegistryException("Failed to connect to the zookeeper server.");
    }
  }

  @Override
  protected boolean doRegister(ServiceInstance serviceInstance) {
    String path = buildProviderPath(serviceInstance, true);
    boolean result = true;
    try {
      zkClient.create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.EPHEMERAL)
          .forPath(path, EMPTY_DATA);
    }
    catch (Exception e) {
      LOGGER.error("Error while registering service to zookeeper.", e);
      result = false;
    }
    return result;
  }

  /**
   * 构建类似/{applicationName}:{serviceInterface}:{version}/providers/{ipAddress}:{port}的路径
   */
  private static String buildProviderPath(ServiceInstance serviceInstance, boolean provider) {
    StringBuilder pathBuilder = new StringBuilder(64)
        .append("/")
        .append(serviceInstance.getApplicationName())
        .append(":")
        .append(serviceInstance.getName())
        .append(":")
        .append(serviceInstance.getVersion())
        .append("/providers");
    if (provider) {
      pathBuilder.append("/")
          .append(serviceInstance.getIpAddress())
          .append(":")
          .append(serviceInstance.getPort());
    }
    return pathBuilder.toString();
  }

  @Override
  protected void doUnregister(ServiceInstance serviceInstance) {

  }

  @Override
  protected void connect(CountDownLatch syncConnect, HostPortPair hostAndPort, HostPortPair... more) {
    String connectString = getZookeeperConnectString(hostAndPort, more);
    zkClient = CuratorFrameworkFactory.builder()
        .namespace(ZKUtils.QRPC_SERVICE_REGISTRY_TOP_LEVEL_NAME)
        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
        .connectString(connectString)
        .sessionTimeoutMs((int) TimeUnit.MINUTES.toMillis(30L)) // session超时时间为30分钟
        .build();
    zkClient.getConnectionStateListenable().addListener((zkCli, state) -> {
      if (state == ConnectionState.CONNECTED) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Successfully connected to the zookeeper server.");
        }
        syncConnect.countDown();
      }
      else if (state == ConnectionState.RECONNECTED) {
        // 重新注册所有服务
        reRegister();
      }
      else if (state == ConnectionState.LOST) {
        // 重新连接zookeeper服务器
        connectToServiceRegistryServer();
        // 重新注册所有服务
        reRegister();
      }
    });
    zkClient.start();
  }

  @Override
  protected void doClose() {
    zkClient.close();
    zkClient = null;
  }

  /**
   * 拼装{@code zookeeper}的{@code connectString}字符串
   * @param hostAndPort ip地址和端口
   * @param more 其他集群的节点的ip地址和端口
   * @return {@code connectString}
   */
  private static String getZookeeperConnectString(HostPortPair hostAndPort, HostPortPair... more) {
    StringJoiner connectStringJoiner = new StringJoiner(",");
    connectStringJoiner.add(toZookeeperSingleNodeConnectString(hostAndPort));
    if (Objects.nonNull(more) && more.length > 0) {
      for (HostPortPair hostPortPair : more) {
        connectStringJoiner.add(toZookeeperSingleNodeConnectString(hostPortPair));
      }
    }
    return connectStringJoiner.toString();
  }

  /**
   * 拼装单个节点的zookeeper的{@code connectString}
   * @param hostAndPort ip地址和端口的{@code HostPortPair}对象
   * @return {@code connectString}字符串
   */
  private static String toZookeeperSingleNodeConnectString(HostPortPair hostAndPort) {
    return new StringBuilder(hostAndPort.getHost())
        .append(":")
        .append(hostAndPort.getPort())
        .toString();
  }

  @Override
  public List<ServiceInstance> subscribeServiceInstances(SubscribeRequest subscribeRequest) {
    ServiceInstance serviceInstance = subscribeRequestToServiceInstance(subscribeRequest);
    String path = buildProviderPath(serviceInstance, false);
    Stat pathStat;
    try {
      pathStat = zkClient.checkExists().forPath(path);
    }
    catch (Exception e) {
      LOGGER.error("Query path " + path + " error.", e);
      throw new ServiceRegistryException(e);
    }
    if (Objects.isNull(pathStat)) {
      throw new ServiceRegistryException("Path " + path + " not exist.");
    }
    List<String> ipPorts;
    try {
      ipPorts = zkClient.getChildren().forPath(path);
    }
    catch (Exception e) {
      LOGGER.error("Query path " + path + " error.", e);
      throw new ServiceRegistryException(e);
    }
    return convertToServiceInstances(ipPorts, subscribeRequest);
  }

  private List<ServiceInstance> convertToServiceInstances(List<String> ipPorts, SubscribeRequest subscribeRequest) {
    if (ipPorts.isEmpty()) {
      return List.of();
    }
    List<ServiceInstance> serviceInstances = new ArrayList<>(ipPorts.size());
    for (String ipPort : ipPorts) {
      ServiceInstance serviceInstance = new ServiceInstance();
      serviceInstance.setApplicationName(subscribeRequest.getApplicationName());
      serviceInstance.setName(subscribeRequest.getName());
      serviceInstance.setVersion(subscribeRequest.getVersion());
      String[] s = ipPort.split(":");
      serviceInstance.setIpAddress(s[0]);
      serviceInstance.setPort(Integer.parseInt(s[1]));
      serviceInstances.add(serviceInstance);
    }
    return serviceInstances;
  }

  /**
   * 将{@code SubscribeRequest}对象转换为{@code ServiceInstance}对象
   * @param request 请求对象
   * @return 转换后的{{@code ServiceInstance}对象
   */
  private static ServiceInstance subscribeRequestToServiceInstance(SubscribeRequest request) {
    ServiceInstance instance = new ServiceInstance();
    instance.setApplicationName(request.getApplicationName());
    instance.setName(request.getName());
    instance.setVersion(request.getVersion());
    return instance;
  }
}
