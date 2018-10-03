package com.qiuyj.qrpc.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-10-03
 */
public class ServiceRegistryTest {

  private static final String TOP_LEVEL_PATH = "/";

  @Test
  public void testZookeeper() {
    try (CuratorFramework zkClient = CuratorFrameworkFactory.builder()
        .namespace("qrpc")
        .connectString("192.168.0.3:2181")
        .retryPolicy(new RetryOneTime(500))
        .build()) {
      zkClient.start();
      List<String> children = zkClient.getChildren().forPath(TOP_LEVEL_PATH);
      deleteAll(zkClient, TOP_LEVEL_PATH, children);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void deleteAll(CuratorFramework zkClient, String parent, List<String> children) throws Exception {
    if (Objects.nonNull(children) && !children.isEmpty()) {
      for (String child : children) {
        String path = getPath(parent, child);
        List<String> subChildren = zkClient.getChildren().forPath(path);
        deleteAll(zkClient, path, subChildren);
      }
    }
    if (!TOP_LEVEL_PATH.equals(parent)) {
      zkClient.delete().forPath(parent);
    }
  }

  private String getPath(String parent, String child) {
    StringBuilder pathBuilder = new StringBuilder();
    if (parent.endsWith("/")) {
      pathBuilder.append(parent)
          .append(child);
    }
    else {
      pathBuilder.append(parent)
          .append("/")
          .append(child);
    }
    return pathBuilder.toString();
  }
}