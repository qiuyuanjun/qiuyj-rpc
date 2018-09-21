package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.commons.protocol.MessageType;
import com.qiuyj.qrpc.commons.protocol.RequestInfo;
import io.netty.channel.Channel;

/**
 * 对rquestInfo进行包装，提供当前requestInfo所属的channel的获取方法
 * @author qiuyj
 * @since 2018-09-18
 */
public class NettyChannelAttachedRequestInfo extends RequestInfo {

  private static final long serialVersionUID = 764110566377818179L;

  /** 异步调用的通信channel */
  private Channel ch;

  /** 消息类型，用于帮助判断是否是异步调用 */
  private MessageType messageType;

  public NettyChannelAttachedRequestInfo(RequestInfo requestInfo, Channel ch) {
    this.ch = ch;
    setInterfaceName(requestInfo.getInterfaceName());
    setMethodName(requestInfo.getMethodName());
    setRequestId(requestInfo.getRequestId());
    setMethodParameters(requestInfo.getMethodParameters());
  }

  public NettyChannelAttachedRequestInfo(RequestInfo requestInfo) {
    this(requestInfo, null);
  }

  public void setChannel(Channel ch) {
    this.ch = ch;
  }

  public Channel getChannel() {
    return ch;
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }
}