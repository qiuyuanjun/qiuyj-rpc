package com.qiuyj.qrpc.codec.protocol;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public enum MessageType {

  /**
   * 错误类型的返回
   */
  ERROR_RESPONSE((byte) 0x01),

  /**
   * rpc请求类型
   */
  RPC_REQUEST((byte) 0x1A),

  /**
   * rpc响应类型
   */
  RPC_RESPONSE((byte) 0x2B),

  /**
   * 心跳请求类型
   */
  HEARTBEAT_REQUEST((byte) 0x3C),

  /**
   * 心跳响应类型
   */
  HEARTBEAT_RESPONSE((byte) 0x4D),

  /**
   * 异步返回类型（表示此时返回的消息已经有服务调用的结果）
   */
  ASYNC_RESPONSE((byte) 0xFF),

  /**
   * 异步返回类型（表示立即返回类型，即此时返回的消息还没有服务调用的结果）
   */
  ASYNC_RESPONSE_IMMEDIATELY((byte) 0xFE),

  /**
   * 异步请求类型
   */
  ASYNC_REQUEST((byte) 0xFD);

  private final byte b;

  MessageType(byte b) {
    this.b = b;
  }

  public byte getMessageTypeByte() {
    return b;
  }

  /**
   * 判断当前报文类型是否是请求类型的报文
   * @return {@code true}是请求报文类型，{@code false}不是请求报文类型
   */
  public boolean isRequestType() {
    return this == RPC_REQUEST ||
        this == ASYNC_REQUEST ||
        this == HEARTBEAT_REQUEST;
  }

  /**
   * 判断当前报文类型是否是响应报文类型
   * @return {@code true}是响应报文类型，{@code false}不是响应报文类型
   */
  public boolean isResponseType() {
    return this == RPC_RESPONSE ||
        this == ASYNC_RESPONSE ||
        this == ASYNC_RESPONSE_IMMEDIATELY ||
        this == HEARTBEAT_RESPONSE ||
        this == ERROR_RESPONSE;
  }

  public static MessageType ofByte(byte b) {
    for (MessageType mt : MessageType.values()) {
      if (mt.getMessageTypeByte() == b) {
        return mt;
      }
    }
    throw new IllegalArgumentException("Unknow message type.");
  }
}
