package com.qiuyj.qrpc.commons;

/**
 * @author qiuyj
 * @since 2018-09-04
 */
public enum ErrorReason {

  SERVICE_NOT_FOUND(1001, "找不到服务"),

  EXECUTE_SERVICE_ERROR(1002, "服务执行异常"),

  ABNORMAL_BUSINESS_ERROR(1003, "非正常业务的异常"),

  ASYNC_EXECUTE_SERVICE_ERROR(1004, "异步服务执行结果异常");

  private final int errorCode;

  private final String errorMessage;

  ErrorReason(int errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * 得到错误信息
   */
  public String toErrorString() {
    return new StringBuilder(this.toString())
        .append("[")
        .append(this.getErrorCode())
        .append(":")
        .append(this.getErrorMessage())
        .append("]")
        .toString();
  }
}