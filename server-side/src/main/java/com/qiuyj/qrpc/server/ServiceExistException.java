package com.qiuyj.qrpc.server;

/**
 * @author qiuyj
 * @since 2018-06-13
 */
public class ServiceExistException extends RuntimeException {

  private static final long serialVersionUID = -5642213479960907535L;

  public ServiceExistException(ClassInstanceValue<?> serviceValue) {
    super("Service[" + serviceValue.getServiceInterface() + "@" + serviceValue.hashCode() + "] already exists.");
  }

  public ServiceExistException(Class<?> serviceInterface) {
    super("Service[" + serviceInterface + "] already exists.");
  }
}
