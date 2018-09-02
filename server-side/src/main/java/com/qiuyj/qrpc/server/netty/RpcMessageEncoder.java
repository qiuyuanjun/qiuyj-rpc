package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.codec.Codec;
import com.qiuyj.qrpc.codec.CodecUtils;
import com.qiuyj.qrpc.codec.RpcMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcMessageEncoder extends MessageToMessageEncoder<RpcMessage> {

  @Override
  protected void encode(ChannelHandlerContext ctx, RpcMessage msg, List<Object> out) {
    Codec codec = CodecUtils.DEFAULT_CODEC;
    out.add(Unpooled.copiedBuffer(codec.encode(msg)));
  }
}
