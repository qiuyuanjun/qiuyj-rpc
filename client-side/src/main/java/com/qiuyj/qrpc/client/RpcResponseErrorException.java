package com.qiuyj.qrpc.client;

import com.qiuyj.qrpc.commons.ErrorReason;

/**
 * @author qiuyj
 * @since 2018-09-08
 */
public class RpcResponseErrorException extends RuntimeException {

  private static final long serialVersionUID = 8264304108947614625L;

  public RpcResponseErrorException(ErrorReason errorReason) {
    super(errorReason.toErrorString());
  }
}
