package com.qiuyj.qrpc.codec;

import com.qiuyj.qrpc.commons.protocol.RpcMessage;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
public interface Codec {

  /**
   * 将字节数组解码成{@code RpcMessage}对象
   * @param b 字节数组
   * @return 对应的{{@code RpcMessage}对象
   */
  RpcMessage decode(byte[] b);

  /**
   * 将{@code RpcMessage}对象编码成网络底层传输的字节数组
   * @param message {@code RpcMessage}对象
   * @return 对应的字节数组
   */
  byte[] encode(RpcMessage message);
}
