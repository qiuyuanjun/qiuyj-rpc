package com.qiuyj.qrpc.commons;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcException extends RuntimeException implements RequestIdCapable {

  private static final long serialVersionUID = 8575711508645312258L;

  private String requestId;

  private ErrorReason errorReason;

  public RpcException() {
    super();
  }

  public RpcException(String requestId, ErrorReason errorReason) {
    super();
    this.requestId = requestId;
    this.errorReason = errorReason;
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public ErrorReason getErrorReason() {
    return errorReason;
  }

  public void setErrorReason(ErrorReason errorReason) {
    this.errorReason = errorReason;
  }
}
