package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.commons.protocol.RequestInfo;
import io.netty.channel.Channel;

/**
 * 对rquestInfo进行包装，提供当前requestInfo所属的channel的获取方法
 * @author qiuyj
 * @since 2018-09-18
 */
public class ChannelAttachedRequestInfo extends RequestInfo {

  private static final long serialVersionUID = -147283483521846080L;

  /** 异步调用的通信channel */
  private Channel ch;

  public ChannelAttachedRequestInfo(RequestInfo requestInfo, Channel ch) {
    this.ch = ch;
    setInterfaceName(requestInfo.getInterfaceName());
    setMethodName(requestInfo.getMethodName());
    setRequestId(requestInfo.getRequestId());
    setMethodParameters(requestInfo.getMethodParameters());
  }

  public ChannelAttachedRequestInfo(RequestInfo requestInfo) {
    this(requestInfo, null);
  }

  public void setChannel(Channel ch) {
    this.ch = ch;
  }

  public Channel getChannel() {
    return ch;
  }
}