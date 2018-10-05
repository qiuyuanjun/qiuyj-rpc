package com.qiuyj.qrpc.registry;

/**
 * 专门用于服务注册中心抛出的异常
 * @author qiuyj
 * @since 2018-10-05
 */
public class ServiceRegistryException extends RuntimeException {

  private static final long serialVersionUID = -8643572690198459675L;

  public ServiceRegistryException() {
    super();
  }

  public ServiceRegistryException(String message) {
    super(message);
  }

  public ServiceRegistryException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceRegistryException(Throwable cause) {
    super(cause);
  }

  protected ServiceRegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
