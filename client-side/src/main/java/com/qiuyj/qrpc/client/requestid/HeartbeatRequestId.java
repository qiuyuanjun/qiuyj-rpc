package com.qiuyj.qrpc.client.requestid;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 专门为心跳类型创建requestid的类
 * @author qiuyj
 * @since 2018-09-08
 */
public class HeartbeatRequestId implements RequestId {

  public static HeartbeatRequestId INSTANCE = new HeartbeatRequestId();

  private final AtomicLong nextSequence = new AtomicLong(0);

  private static final String HEARTBEAT_PREFIX = "heartbeat$";

  private HeartbeatRequestId() {
    // for private
  }

  @Override
  public String nextRequestId() {
    return HEARTBEAT_PREFIX + nextSequence.getAndIncrement();
  }
}
