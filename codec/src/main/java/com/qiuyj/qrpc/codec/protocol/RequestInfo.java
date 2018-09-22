package com.qiuyj.qrpc.codec.protocol;

import java.io.Serializable;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
public class RequestInfo implements Serializable {

  private static final long serialVersionUID = 2103062965812918099L;

  /** 当前请求的id，客户端设置 */
  private String requestId;

  /**
   * 调用的接口名
   */
  private String interfaceName;

  /**
   * 调用的方法名
   */
  private String methodName;

  /**
   * 对应的参数，可以为null
   */
  private Object[] methodParameters;

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public Object[] getMethodParameters() {
    return methodParameters;
  }

  public void setMethodParameters(Object[] methodParameters) {
    this.methodParameters = methodParameters;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
