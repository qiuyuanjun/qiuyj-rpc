package com.qiuyj.qrpc.registry.zookeeper;

import com.qiuyj.qrpc.registry.AbstractServiceRegistry;
import com.qiuyj.qrpc.registry.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author qiuyj
 * @since 2018-10-04
 */
public class ZookeeperServiceRegistry extends AbstractServiceRegistry {

  /** curator客户端 */
  private CuratorFramework zkClient;

  @Override
  protected boolean doRegister(ServiceInstance serviceInstance) {

    return true;
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
  }

  @Override
  protected void doClose() {
    zkClient.close();
  }

  /**
   * 拼装{@code zookeeper}的{@code connectString}字符串
   * @param hostAndPort ip地址和端口
   * @param more 其他集群的节点的ip地址和端口
   * @return {@code connectString}
   */
  private static String getZookeeperConnectString(HostPortPair hostAndPort, HostPortPair... more) {
    StringJoiner connectStringJoiner = new StringJoiner(",");
    connectStringJoiner.add(hostAndPort.getHost() + ":" + hostAndPort.getPort());
    if (Objects.nonNull(more) && more.length > 0) {
      for (HostPortPair hostPortPeer : more) {
        connectStringJoiner.add(hostPortPeer.getHost() + ":" + hostPortPeer.getPort());
      }
    }
    return connectStringJoiner.toString();
  }
}
