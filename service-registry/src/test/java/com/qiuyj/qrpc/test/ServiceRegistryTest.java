package com.qiuyj.qrpc.test;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author qiuyj
 * @since 2018-10-03
 */
public class ServiceRegistryTest {

  private static final String TOP_LEVEL_PATH = "/";

  private CuratorFramework zkClient;

//  @Before
  public void before() {
    zkClient = CuratorFrameworkFactory.builder()
        .connectString("192.168.0.3:2181")
        .retryPolicy(new RetryOneTime(500))
        .build();
    zkClient.start();
  }

  @Test
  public void testZookeeper() {
    try {
      List<String> children = zkClient.getChildren().forPath(TOP_LEVEL_PATH);
      deleteAll(zkClient, TOP_LEVEL_PATH, children);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      zkClient.close();
    }
  }

  @Test
  public void testListener() {
    PathChildrenCache childrenCache = new PathChildrenCache(zkClient, "/qrpc", true);
    childrenCache.getListenable().addListener((zkCli, event) -> {
      System.out.println(event.getType());
    });
    try {
      childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
      zkClient.create().withMode(CreateMode.EPHEMERAL).forPath("/qrpc/qiuyj", new byte[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      try {
        childrenCache.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      zkClient.close();
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

  @Test
  public void testOrigin() throws IOException, InterruptedException, KeeperException {
    CountDownLatch latch = new CountDownLatch(1);
    ZooKeeper zooKeeper = new ZooKeeper("192.168.0.3:2181", 30000, new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
          latch.countDown();
        }
      }
    });
    latch.await();
    System.out.println("连接成功");
    zooKeeper.exists("/qrpc/qiuyj", new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        System.out.println(event.getType() + " - " + event.getPath());
      }
    });
    zooKeeper.create("/qrpc/qiuyj", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    zooKeeper.exists("/qrpc/qiuyj", new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        System.out.println(event.getType() + " - " + event.getPath());
      }
    });
    zooKeeper.close();
  }
}