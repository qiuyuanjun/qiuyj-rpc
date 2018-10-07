package com.qiuyj.qrpc.registry;

import com.qiuyj.qrpc.registry.metadata.VersionAndWeightRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
  private static final AtomicReference<ServiceRegistryState> STATE
      = new AtomicReference<>(ServiceRegistryState.SHUTDOWN);

  /**
   * 等待注册到注册中心的服务队列，默认容量为128
   */
  private final BlockingQueue<ServiceInstance> waitingForRegister
      = new ArrayBlockingQueue<>(128);

  /**
   * 等待从注册中心注销的服务队列，默认容量为128
   */
  private final BlockingQueue<ServiceInstance> waitingForUnregister
      = new ArrayBlockingQueue<>(128);

  /** 未注册成功的{@code ServiceInstance}对象 */
  private Set<ServiceInstance> incomplateServiceInstances
      = Collections.newSetFromMap(new ConcurrentHashMap<>(128));

  /** 所有应用所发布的所有服务接口，相当于provider端 */
  private ConcurrentMap<String, List<VersionAndWeightRegistration>> providersMappingApplication
      = new ConcurrentHashMap<>(64);

  /** 所有应用所引用的所有服务接口，相当于consumer端 */
  private ConcurrentMap<String, List<VersionAndWeightRegistration>> consumersMappingApplication
      = new ConcurrentHashMap<>(64);

  protected AbstractServiceRegistry() {
    // 服务注册中心启动，必须判定shutdown为true
    if (STATE.compareAndSet(ServiceRegistryState.SHUTDOWN, ServiceRegistryState.RUNNING)) {
      // 从配置文件读取注册中心的host和port
      String host = "192.168.0.3";
      int port = 2181;
      // 连接服务注册中心
      connect(new HostPortPair(host, port));
      NamedThreadFactory threadFactory = new NamedThreadFactory();
      // 开启服务注册的线程
      startRegisterServiceThread(threadFactory);
      // 开启服务注销的线程
      startUnregisterServiceThread(threadFactory);
    }
    else {
      throw new IllegalStateException("Only one service registry can be created. Except STATE[SHUTDOWN],but STATE[" + STATE + "]");
    }
  }

  /**
   * 单独开一条线程，内部判断{@code STATE}的状态，如果是{@link ServiceRegistryState#RUNNING}
   * 那么，内部一直循环，并且从阻塞队列里面获取待注册的服务，注册到服务注册中心
   * @param threadFactory 线程工厂
   */
  private void startRegisterServiceThread(NamedThreadFactory threadFactory) {
    threadFactory.newThread(() -> {
      // 允许被中断的次数为三次
      int timesThatAllowedToBeInterrupted = 3;
      while (AbstractServiceRegistry.STATE.get() == AbstractServiceRegistry.ServiceRegistryState.RUNNING) {
        // 从阻塞队列里面取出一个待注册的服务来，如果阻塞队列里面没有待注册的服务
        // 那么这里将一直阻塞
        ServiceInstance serviceInstance = null;
        try {
          serviceInstance = AbstractServiceRegistry.this.waitingForRegister.take();
        }
        catch (InterruptedException e) {
          if (timesThatAllowedToBeInterrupted-- > 0) {
            AbstractServiceRegistry.LOGGER.error("Error while taking element from blocking queue.", e);
          }
          else {
            Thread.currentThread().interrupt();
          }
        }
        if (Objects.nonNull(serviceInstance) && serviceInstance != ServiceInstance.EMPTY_SERVICE_INSTANCE) {
          if (AbstractServiceRegistry.this.doRegister(serviceInstance)) {
            List<VersionAndWeightRegistration> registrations
                = AbstractServiceRegistry.this.providersMappingApplication.computeIfAbsent(
                    serviceInstance.getApplicationName(), (key) -> new ArrayList<>());
            synchronized (registrations) {
              registrations.add(serviceInstance);
            }
            AbstractServiceRegistry.this.incomplateServiceInstances.remove(serviceInstance);
          }
          else {
            AbstractServiceRegistry.this.incomplateServiceInstances.add(serviceInstance);
          }
        }
      }
    }, "Register-service-thread").start();
  }

  /**
   * 单独开一条线程，内部判断{@code STATE}的状态，如果是{@link ServiceRegistryState#RUNNING}
   * 那么，内部一直循环，并且从阻塞队列里面获取待注销的服务，从服务注册中心注销掉
   * @param threadFactory 线程工厂
   */
  private void startUnregisterServiceThread(NamedThreadFactory threadFactory) {
    threadFactory.newThread(() -> {
      // 允许被中断的次数为三次
      int timesThatAllowedToBeInterrupted = 3;
      while (AbstractServiceRegistry.STATE.get() == AbstractServiceRegistry.ServiceRegistryState.RUNNING) {
        ServiceInstance serviceInstance = null;
        try {
          serviceInstance = AbstractServiceRegistry.this.waitingForUnregister.take();
        }
        catch (InterruptedException e) {
          if (timesThatAllowedToBeInterrupted-- > 0) {
            AbstractServiceRegistry.LOGGER.error("Error while taking element from blocking queue.", e);
          }
          else {
            Thread.currentThread().interrupt();
          }
        }
        if (Objects.nonNull(serviceInstance) && serviceInstance != ServiceInstance.EMPTY_SERVICE_INSTANCE) {
          AbstractServiceRegistry.this.doUnregister(serviceInstance);
          // registrations一定不为null
          List<VersionAndWeightRegistration> registrations = AbstractServiceRegistry.this.providersMappingApplication.get(serviceInstance.getApplicationName());
          synchronized (registrations) {
            registrations.remove(serviceInstance);
          }
          AbstractServiceRegistry.this.incomplateServiceInstances.remove(serviceInstance);
        }
      }
    }, "Unregister-service-thread").start();
  }

  /**
   * 具体的服务注册的实现方法，交给对应的子类去实现
   * @param serviceInstance 待注册的服务信息
   */
  protected abstract boolean doRegister(ServiceInstance serviceInstance);

  /**
   * 具体的服务注销的实现方法，交给对应的子类去实现
   * @param serviceInstance 待注销的服务信息
   */
  protected abstract void doUnregister(ServiceInstance serviceInstance);

  /**
   * 连接服务注册中心，交给具体的子类处理
   * @param hostAndPort 服务注册中心的ip地址和端口映射对象
   * @param more 如果是集群，那么这里表示其他机器的ip地址和端口映射对象
   */
  protected abstract void connect(HostPortPair hostAndPort, HostPortPair... more);

  @Override
  public List<ServiceInstance> getProvidersByApplicationName(String applicationName) {
    return fromMapping(applicationName, providersMappingApplication);
  }

  /**
   * 从给定的map容器里面得到对应的应用所持有的{@code ServiceInstance}集合
   * @param applicationName 应用名
   * @param mapping map容器
   * @return {@code ServiceInstance}集合
   */
  private static List<ServiceInstance> fromMapping(String applicationName, ConcurrentMap<String, List<VersionAndWeightRegistration>> mapping) {
    Objects.requireNonNull(applicationName, "applicationName == null");
    List<VersionAndWeightRegistration> registrations = mapping.get(applicationName);
    List<ServiceInstance> serviceInstances = null;
    if (Objects.nonNull(registrations)) {
      synchronized (registrations) {
        if (!registrations.isEmpty()) {
          serviceInstances = new ArrayList<>(registrations.size());
          for (VersionAndWeightRegistration registration : registrations) {
            serviceInstances.add(new ServiceInstance(registration, applicationName));
          }
        }
      }
    }
    if (Objects.isNull(serviceInstances)) {
      // from java9
      serviceInstances = List.of();
    }
    return serviceInstances;
  }

  @Override
  public List<ServiceInstance> getConsumersByApplicationName(String applicationName) {
    return fromMapping(applicationName, consumersMappingApplication);
  }

  @Override
  public void register(ServiceInstance serviceInstance) {
    Objects.requireNonNull(serviceInstance, "serviceInstance == null");
    boolean exist = false;
    List<VersionAndWeightRegistration> registrations = providersMappingApplication.get(serviceInstance.getApplicationName());
    if (Objects.nonNull(registrations)) {
      synchronized (registrations) {
        exist = registrations.contains(serviceInstance);
      }
    }
    if (exist || incomplateServiceInstances.contains(serviceInstance) || waitingForRegister.contains(serviceInstance)) {
      throw new ServiceRegistryException("The service to be registered already exists.");
    }
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
    Objects.requireNonNull(serviceInstance, "serviceInstance == null");
    boolean notExist = true;
    List<VersionAndWeightRegistration> registrations = providersMappingApplication.get(serviceInstance.getApplicationName());
    if (Objects.nonNull(registrations)) {
      synchronized (registrations) {
        notExist = registrations.contains(serviceInstance);
      }
    }
    if (notExist && !incomplateServiceInstances.contains(serviceInstance)) {
      throw new ServiceRegistryException("The service to be unregistered not exists.");
    }
    try {
      waitingForUnregister.put(serviceInstance);
    }
    catch (InterruptedException e) {
      // 这种情况一般不会发生
      LOGGER.error("Error while puting element to blocking queue.", e);
    }
  }

  @Override
  public void close() {
    waitingForRegister.clear();
    waitingForUnregister.clear();
    incomplateServiceInstances.clear();
    providersMappingApplication.clear();
    consumersMappingApplication.clear();
    doClose();
    STATE.getAndSet(ServiceRegistryState.SHUTDOWN);

    /*
     * 由于注册线程会一直阻塞
     * 所以在服务注册中心关闭的时候，注册线程会一直阻塞
     * 所以关闭的时候需要手动往阻塞队列里面加入这个对象从而唤醒被阻塞的线程
     */
    register(ServiceInstance.EMPTY_SERVICE_INSTANCE);
    unregister(ServiceInstance.EMPTY_SERVICE_INSTANCE);
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
