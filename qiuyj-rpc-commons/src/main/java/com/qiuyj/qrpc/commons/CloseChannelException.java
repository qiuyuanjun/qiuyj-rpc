package com.qiuyj.qrpc.commons;

/**
 * 关闭channel的异常，当抛出这个异常的时候，服务器端要主动关闭和客户端的连接
 * 当有以下几种情况发生的时候，应当要抛出该异常
 *  1. 当服务器端解码客户端发送过来的数据的时候，如果发现数据要求不符合规定，（可能是传输的中途被篡改）
 * @author qiuyj
 * @since 2018-09-04
 */
public class CloseChannelException extends RuntimeException {

  private static final long serialVersionUID = -2317264483865196420L;

  public CloseChannelException(String msg) {
    super(msg);
  }
}
