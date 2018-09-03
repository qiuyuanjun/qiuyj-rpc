package com.qiuyj.qrpc.client.requestid;

import java.util.UUID;

/**
 * uuid形式的requestid生成器
 * @author qiuyj
 * @since 2018-09-03
 */
public class UUIDRequestId implements RequestId {

  @Override
  public String nextRequestId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
