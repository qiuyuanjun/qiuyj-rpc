package com.qiuyj.qrpc.codec.protocol;

import java.io.Serializable;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
public class ResponseInfo implements Serializable {

  private static final long serialVersionUID = -4611490424395924027L;

  /** requestId */
  private String requestId;

  /**
   * 请求结果（可以为null，也可以是一个异常对象）
   */
  private Object result;

  /**
   * 是否已经计算出结果（主要用于异步调用）
   */
  private boolean done;

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
    this.done = true;
  }

  public boolean hasError() {
    return result instanceof Throwable;
  }

  public Throwable getCause() {
    return hasError() ? (Throwable) result : null;
  }

  public boolean isDone() {
    return done;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
