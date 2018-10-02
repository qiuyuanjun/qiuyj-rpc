package com.qiuyj.api.client;

import com.qiuyj.api.Connection;
import com.qiuyj.api.Ipv4Utils;
import com.qiuyj.api.ThrowableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public abstract class AbstractClient implements ConfigurableClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

  /** 客户端和服务器端之间的连接对象，如果和远程服务器端没有连接，那么该对象应该为null */
  private Connection connection;

  /** 当前客户端是否已经链接到了远程服务器 */
  private volatile boolean connected;

  /** 当客户端和服务器端连接失败的时候的最大尝试连接次数，不包括第一次连接 */
  // 默认最大尝试连接次数为1次
  private int maxRetry = 1;

  protected Connection getConnection() {
    return connection;
  }

  @Override
  public void connect() {
    if (connected) {
      LOGGER.warn("The client has been already connected to the remote server.");
    }
    else {
      boolean firstConnect = false;
      synchronized (this) {
        if (!connected) {
          int retry = 0;
          for (;;) {
            try {
              connection = doConnect();
              break;
            }
            catch (Throwable t) {
              if (++retry > maxRetry) {
                // 如果最大重试连接都没能连接上远程服务器，那么抛出异常
                ThrowableUtils.rethrowException(t);
              }
              else {
                // 记录异常日志
                LOGGER.warn("Failed to connect to the remote server and start the [" + retry + "] reconnectiton attempt.", t);
              }
            }
          }
          if (Objects.isNull(connection)) {
            throw new IllegalStateException("connection == null.");
          }
          firstConnect = connected = true;
        }
      }
      if (firstConnect) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          if (AbstractClient.this.connected) {
            AbstractClient.this.close();
          }
        }));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Connect to the remote server successfully. At " + LocalDateTime.now());
        }
        afterConnected(connection);
      }
    }
  }

  /**
   * 成功连接到远程服务器之后
   * 模版方法，如果子类有这样的需求，那么可以重写该方法以实现对应的需求
   * @param connection 客户端和服务器端之间的连接对象
   */
  protected void afterConnected(Connection connection) {
    // for subclass
  }

  /**
   * 具体的实现连接远程服务器的方法，交给对应的子类实现
   * @return {@code Connection}客户端和远程服务端之间的连接对象
   */
  protected abstract Connection doConnect();

  @Override
  public void reconnect() {
    if (connected) {
      close();
    }
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Start re-connect to the remote server.");
    }
    connect();
  }

  @Override
  public void close() {
    if (!connected) {
      LOGGER.warn("The client is already been closed from remote server.");
    }
    else {
      synchronized (this) {
        if (connected) {
          doClose();
          connection.close();
          connection = null;
          connected = false;
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Close the connection between the client and the remote server successfully. At " + LocalDateTime.now());
          }
        }
      }
    }
  }

  @Override
  public InetAddress getLocalAddress() {
    return Ipv4Utils.getLocalInetAddress();
  }

  /**
   * 交给子类实现的关闭方法，子类可以用这个方法实现一些资源的清理
   */
  protected abstract void doClose();

  @Override
  public void setMaxRetryWhenFailedToConnect(int maxRetry) {
    if (maxRetry >= 0) {
      this.maxRetry = maxRetry;
    }
  }
}