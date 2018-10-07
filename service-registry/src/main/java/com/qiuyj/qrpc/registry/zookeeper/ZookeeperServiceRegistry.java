package com.qiuyj.qrpc.registry.zookeeper;

import com.qiuyj.qrpc.registry.AbstractServiceRegistry;
import com.qiuyj.qrpc.registry.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;

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
      throw new IllegalStateException("Failed to connect to the zookeeper server.");
    }
  }

  @Override
  protected boolean doRegister(ServiceInstance serviceInstance) {
    String path = buildProviderPath(serviceInstance);
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

  private static String buildProviderPath(ServiceInstance serviceInstance) {
    return new StringBuilder(64)
        .append("/")
        .append(serviceInstance.getApplicationName())
        .append("/provider/")
        .append(serviceInstance.getName())
        .append(":")
        .append(serviceInstance.getVersion())
        .append("/")
        .append(serviceInstance.getIpAddress())
        .append(":")
        .append(serviceInstance.getPort())
        .toString();
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
        .build();
    zkClient.getConnectionStateListenable().addListener((zkCli, state) -> {
      if (state == ConnectionState.CONNECTED) {
        syncConnect.countDown();
      }
      else if (state == ConnectionState.RECONNECTED) {
        // 重新注册所有服务

      }
      else if (state == ConnectionState.LOST) {
        // 重新连接zookeeper服务器
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
}
