package com.qiuyj.api.server;

import com.qiuyj.api.Ipv4Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-06-12
 */
public abstract class AbstractServer implements Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServer.class);

  /** 服务器运行状态 */
  private volatile boolean running;

  private InetAddress localAddress;

  @Override
  public void start() {
    if (isRunning()) {
      LOGGER.warn("The server has been already started.");
//      System.err.println("The server has been already started.");
    }
    else {
      boolean firstStart = false;
      synchronized (this) {
        // 这里需要双重检测服务器的状态，防止在多线程环境下，多条线程多次执行doStart()方法
        if (!isRunning()) {
          doStart();
          firstStart = running = true;
        }
      }
      if (firstStart) {
        // 如果是第一次开启服务器，那么注册对应的关闭钩子程序
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          if (AbstractServer.this.isRunning()) {
            AbstractServer.this.close();
          }
        }));
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Start the server successfully. At " + LocalDateTime.now());
        }
        setLocalAddress();
        afterServerStarted();
      }
    }
  }

  /**
   * 设置服务器的本地ip地址
   */
  private void setLocalAddress() {
    if (Objects.isNull(localAddress)) {
      String serverIp = Ipv4Utils.getLocalAddress();
      try {
        localAddress = InetAddress.getByName(serverIp);
      }
      catch (UnknownHostException e) {
        LOGGER.warn("Error getting server inet address. Use 127.0.0.1 instead.", e);
        localAddress = InetAddress.getLoopbackAddress();
      }
    }
  }

  /**
   * 服务器完全启动之后的回调，如果子类有这样的需求，那么可以通过重写该方法实现
   */
  protected void afterServerStarted() {
    // for subclass
  }

  /**
   * 启动服务器的具体逻辑，交给具体的子类实现
   */
  protected abstract void doStart();

  @Override
  public InetAddress getLocalAddress() {
    return localAddress;
  }

  @Override
  public void close() {
    if (!isRunning()) {
      LOGGER.warn("The server has been already closed.");
//      System.err.println("The server has been already closed.");
    }
    else {
      synchronized (this) {
        // 这里需要双重检测服务器的状态，防止在多线程环境下，多条线程多次执行doClose()方法
        if (isRunning()) {
          doClose();
          running = false;
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Shutdown the server successfully. At: " + LocalDateTime.now());
          }
        }
      }
    }
  }

  /**
   * 关闭服务器的具体逻辑，交给具体的子类实现
   */
  protected abstract void doClose();

  @Override
  public boolean isRunning() {
    return running;
  }
}