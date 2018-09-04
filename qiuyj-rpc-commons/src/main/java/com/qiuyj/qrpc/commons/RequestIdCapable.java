package com.qiuyj.qrpc.commons;

/**
 * 获取requestId的接口
 * @author qiuyj
 * @since 2018-09-04
 */
public interface RequestIdCapable {

  /**
   * 得到requestId
   */
  String getRequestId();
}
