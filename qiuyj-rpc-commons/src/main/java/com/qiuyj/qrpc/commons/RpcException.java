package com.qiuyj.qrpc.commons;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcException extends RuntimeException implements RequestIdCapable {

  private static final long serialVersionUID = -5791393840852493742L;

  private String requestId;

  private ErrorReason errorReason;

  public RpcException(String requestId, ErrorReason errorReason) {
    this.requestId = requestId;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }
}
