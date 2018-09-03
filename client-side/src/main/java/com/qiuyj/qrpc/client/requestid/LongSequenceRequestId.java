package com.qiuyj.qrpc.client.requestid;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qiuyj
 * @since 2018-09-03
 */
public class LongSequenceRequestId implements RequestId {

  private final AtomicLong nextSequence = new AtomicLong(0);

  @Override
  public String nextRequestId() {
    return Long.toString(nextSequence.getAndIncrement());
  }
}