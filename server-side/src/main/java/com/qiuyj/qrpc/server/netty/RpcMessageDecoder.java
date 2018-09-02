package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.codec.*;
import com.qiuyj.qrpc.commons.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
    // 将报文数据转储在自己数组里面
    byte[] b = new byte[msg.readableBytes()];
    msg.getBytes(0, b);
    // 从报文数据里面得到Codec的类型
    Codec codec = CodecUtils.DEFAULT_CODEC;
    // 解码报文数据得到RpcMessage对象
    RpcMessage rpcMessage = codec.decode(b);
    // 验证magic number
    if (rpcMessage.getMagic() != RpcMessage.MAGIC_NUMBER) {
      throw new RpcException();
    }
    // 验证正文长度和正文
    else if ((rpcMessage.getContentLength() > 0 && Objects.isNull(rpcMessage.getContent())) ||
        (rpcMessage.getContentLength() == 0 && Objects.nonNull(rpcMessage.getContent()))) {
      throw new RpcException();
    }
    else {
      out.add(rpcMessage);
    }
  }
}
