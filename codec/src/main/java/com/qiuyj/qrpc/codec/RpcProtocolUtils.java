package com.qiuyj.qrpc.codec;

import com.qiuyj.qrpc.codec.protocol.MessageType;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
abstract class RpcProtocolUtils {

  /**
   * 从字节数组中读取rpc协议规定的魔数
   * @param b 字节数组
   * @return 魔数
   */
  static int readMagicNumber(byte[] b) {
//    assert b.length >= 4 : "Invalid magic number sequence.";
    if (b.length < 4) {
      throw new IllegalArgumentException("Invalid magic number sequence.");
    }
    else {
      // rpc协议的头四个字节代表魔数
      return CodecUtils.b2i(b[0], b[1], b[2], b[3]);
    }
  }

  /**
   * 从字节数组中读取报文长度
   * @param b 字节数组
   * @return 报文长度，必须大于或等于0
   */
  static int readContentLength(byte[] b) {
//    assert b.length >= 9 : "Invalid content length sequence.";
    if (b.length < 9) {
      throw new IllegalArgumentException("Invalid content length sequence.");
    }
    else {
      // rpc协议的第6到9个字节代表正文长度
      int contentLength = CodecUtils.b2i(b[5], b[6], b[7], b[8]);
//      assert contentLength >= 0 : "Content length cannot be negative.";
      // 如果为负数，那么取长度为0
      return contentLength < 0 ? 0 : contentLength;
    }
  }

  /**
   * 读取报文类型
   * @param b 报文内容
   * @return 报文类型
   */
  static MessageType readMessageType(byte[] b) {
    if (b.length < 5) {
      throw new IllegalStateException("Invalid message type sequence.");
    }
    else {
      // rpc协议的第五个字节代表报文标志位
      return MessageType.ofByte(b[4]);
    }
  }

}
