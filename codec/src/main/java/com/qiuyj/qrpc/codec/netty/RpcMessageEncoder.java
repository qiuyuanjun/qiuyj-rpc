package com.qiuyj.qrpc.codec.netty;

import com.qiuyj.qrpc.codec.Codec;
import com.qiuyj.qrpc.codec.CodecUtils;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> /*MessageToMessageEncoder<RpcMessage>*/ {

//  @Override
//  protected void encode(ChannelHandlerContext ctx, RpcMessage msg, List<Object> out) {
//    Codec codec = CodecUtils.DEFAULT_CODEC;
//    out.add(Unpooled.copiedBuffer(codec.encode(msg)));
//  }

  @Override
  protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) {
    Codec codec = CodecUtils.DEFAULT_CODEC;
    byte[] b = codec.encode(msg);
    out.writeBytes(b);
  }
}
