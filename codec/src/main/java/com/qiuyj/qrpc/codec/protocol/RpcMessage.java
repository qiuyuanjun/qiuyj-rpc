package com.qiuyj.qrpc.codec.protocol;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
public class RpcMessage {

  public static final int MAGIC_NUMBER = 0x19961122;

  /** 报文头长度，不包括报文正文 */
  public static final int HEADER_LENGTH = 9;

  /** 魔数，4个字节 */
  private int magic;

  /** 报文类型，一个字节 */
  private MessageType messageType;

  /** 正文长度，4个字节 */
  private int contentLength;

  /** 正文内容，值为{@code RequestInfo}和{@code ResponseInfo}两种 */
  private Object content;

  public int getMagic() {
    return magic;
  }

  public void setMagic(int magic) {
    this.magic = magic;
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  public int getContentLength() {
    return contentLength;
  }

  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  public Object getContent() {
    return content;
  }

  public void setContent(Object content) {
    this.content = content;
  }

}
