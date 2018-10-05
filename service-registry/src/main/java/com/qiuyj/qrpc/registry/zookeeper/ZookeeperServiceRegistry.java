package com.qiuyj.qrpc.registry.zookeeper;

import com.qiuyj.qrpc.registry.AbstractServiceRegistry;
import com.qiuyj.qrpc.registry.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.StringJoiner;

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
  protected boolean doRegister(ServiceInstance serviceInstance) {
    // TODO: 构建path路径
    String path = "";
    boolean result = true;
    try {
      zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, EMPTY_DATA);
    }
    catch (Exception e) {
      LOGGER.error("Error while registering service to zookeeper.", e);
      result = false;
    }
    return result;
  }

  @Override
  protected void doUnregister(ServiceInstance serviceInstance) {

  }

  @Override
  protected void connect(HostPortPair hostAndPort, HostPortPair... more) {
    String connectString = getZookeeperConnectString(hostAndPort, more);
    zkClient = CuratorFrameworkFactory.builder()
        .namespace(ZKUtils.QRPC_SERVICE_REGISTRY_TOP_LEVEL_NAME)
        .retryPolicy(new RetryOneTime(1000))
        .connectString(connectString)
        .build();
    zkClient.start();
    zkClient.getCuratorListenable().addListener((zkCli, eventType) -> {
      if (eventType.getType() == CuratorEventType.CLOSING) {
        // reconnect

      }
    });
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
