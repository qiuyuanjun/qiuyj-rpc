package com.qiuyj.qrpc.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author qiuyj
 * @since 2018-09-23
 */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceRegistry.class);

  /**
   * 判定当前注册中心运行状态，一个jvm虚拟机只能有一个服务注册中心，不能运行多个服务注册中心
   */
  private static final AtomicReference<ServiceRegistryState> STATE = new AtomicReference<>(ServiceRegistryState.SHUTDOWN);

  /**
   * 等待注册到注册中心的服务队列，默认容量为512
   */
  private final BlockingQueue<ServiceInstance> waitingForRegister = new ArrayBlockingQueue<>(512);

  /** 所有已经注册了的服务的{@code ServiceInstance}对象 */
  private List<ServiceInstance> serviceInstances;

  protected AbstractServiceRegistry() {
    // 服务注册中心启动，必须判定shutdown为true
    if (STATE.compareAndSet(ServiceRegistryState.SHUTDOWN, ServiceRegistryState.RUNNING)) {
      // 从配置文件读取注册中心的host和port
      String host = "192.168.0.3";
      int port = 2181;
      // 连接服务注册中心
      connect(new HostPortPair(host, port));
      serviceInstances = new ArrayList<>();
      // 开启注册服务的线程
      NamedThreadFactory threadFactory = new NamedThreadFactory();
      threadFactory.newThread(() -> {
        // 允许被中断的次数为三次
        int timesThatAllowedToBeInterrupted = 3;
        while (STATE.get() == ServiceRegistryState.RUNNING) {
          // 从阻塞队列里面取出一个待注册的服务来，如果阻塞队列里面没有待注册的服务
          // 那么这里将一直阻塞
          ServiceInstance serviceInstance = null;
          try {
            serviceInstance = waitingForRegister.take();
          }
          catch (InterruptedException e) {
            if (--timesThatAllowedToBeInterrupted > 0) {
              LOGGER.error("Error while taking element from blocking queue.", e);
            }
            else {
              Thread.currentThread().interrupt();
            }
          }
          if (Objects.nonNull(serviceInstance)) {
            doRegister(serviceInstance);
          }
        }
      }, "Loop-to-get-the-registered-service-thread").start();
    }
    else {
      throw new IllegalStateException("Only one service registry can be created. Except STATE[SHUTDOWN]. For STATE[" + STATE + "]");
    }
  }

  /**
   * 具体的服务注册的实现方法，交个对应的子类去实现
   * @param serviceInstance 待注册的服务信息
   */
  protected abstract void doRegister(ServiceInstance serviceInstance);

  /**
   * 连接服务注册中心，交给具体的子类处理
   * @param hostAndPort 服务注册中心的ip地址和端口映射对象
   * @param more 如果是集群，那么这里表示其他机器的ip地址和端口映射对象
   */
  protected abstract void connect(HostPortPair hostAndPort, HostPortPair... more);

  @Override
  public List<ServiceInstance> registeredServiceInstances() {
    return serviceInstances;
  }

  @Override
  public void register(ServiceInstance serviceInstance) {
    try {
      waitingForRegister.put(serviceInstance);
    }
    catch (InterruptedException e) {
      // 这种情况一般不会发生
      LOGGER.error("Error while puting element to blocking queue.", e);
    }
  }

  @Override
  public void unregister(ServiceInstance serviceInstance) {

  }

  @Override
  public void close() {
    waitingForRegister.clear();
    if (Objects.nonNull(serviceInstances)) {
      serviceInstances.clear();
      serviceInstances = null;
    }
    doClose();
    STATE.getAndSet(ServiceRegistryState.SHUTDOWN);
  }

  /**
   * 子类关闭资源的方法
   */
  protected abstract void doClose();

  /**
   * ip地址和端口
   */
  protected static class HostPortPair {

    /** 主机名称，一般指ip地址 */
    private String host;

    /** 端口 */
    private int port;

    public HostPortPair(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public String getHost() {
      return host;
    }

    public int getPort() {
      return port;
    }
  }

  /**
   * 服务注册中心的运行状态
   */
  private enum ServiceRegistryState {
    RUNNING, SHUTDOWN
  }
}
