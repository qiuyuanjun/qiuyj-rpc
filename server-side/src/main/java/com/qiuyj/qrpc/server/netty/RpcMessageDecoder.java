package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.codec.Codec;
import com.qiuyj.qrpc.codec.CodecUtils;
import com.qiuyj.qrpc.codec.RpcMessage;
import com.qiuyj.qrpc.server.CloseChannelException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class RpcMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcMessageDecoder.class);

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
      LOGGER.error("rpc消息的魔数(magic number)不符合要求，可能传输中途被篡改。关闭连接。");
      throw new CloseChannelException("Magic number is illegal.");
    }
    // 验证正文长度和正文
    else if ((rpcMessage.getContentLength() > 0 && Objects.isNull(rpcMessage.getContent())) ||
        (rpcMessage.getContentLength() == 0 && Objects.nonNull(rpcMessage.getContent()))) {
      LOGGER.error("正文的长度和正文的内容所代表的长度不一致，可能是传输中途被篡改。关闭连接。");
      throw new CloseChannelException("Content not match content length.");
    }
    else {
      out.add(rpcMessage);
    }
  }
}
