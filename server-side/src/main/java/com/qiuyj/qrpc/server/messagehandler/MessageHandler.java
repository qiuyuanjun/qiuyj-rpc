package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.qrpc.codec.ResponseInfo;

/**
 * 消息处理器
 * @author qiuyj
 * @since 2018-08-26
 */
public interface MessageHandler<T> {

  /**
   * 处理对应的消息
   * @param message 要处理的消息
   */
  ResponseInfo handle(T message);
}
