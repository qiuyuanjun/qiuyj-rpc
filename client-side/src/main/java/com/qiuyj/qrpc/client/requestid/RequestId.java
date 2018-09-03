package com.qiuyj.qrpc.client.requestid;

/**
 * RequestId生成接口
 * @author qiuyj
 * @since 2018-09-03
 */
public interface RequestId {

  /**
   * 得到下一个requestid
   */
  String nextRequestId();
}
