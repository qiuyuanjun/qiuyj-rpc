package com.qiuyj.qrpc.codec;

/**
 * @author qiuyj
 * @since 2018-09-04
 */
public class SerializationException extends RuntimeException {

  private static final long serialVersionUID = 8743098188755901259L;

  public SerializationException(Throwable t) {
    super(t);
  }
}
