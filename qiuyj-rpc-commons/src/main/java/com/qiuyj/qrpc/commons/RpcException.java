package com.qiuyj.qrpc.commons;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcException extends RuntimeException implements RequestIdCapable {

  private static final long serialVersionUID = -5791393840852493742L;

  private String requestId;

  public RpcException() {
    super();
  }

  public RpcException(String requestId, ErrorReason errorReason) {
    super(getErrorMessage(errorReason));
    this.requestId = requestId;
  }

  private static String getErrorMessage(ErrorReason errorReason) {
    return new StringBuilder(errorReason.toString())
        .append("[")
        .append(errorReason.getErrorCode())
        .append(":")
        .append(errorReason.getErrorMessage())
        .append("]")
        .toString();
  }

  @Override
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

}