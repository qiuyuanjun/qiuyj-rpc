package com.qiuyj.qrpc.client.netty;

import com.qiuyj.qrpc.server.netty.RpcMessageDecoder;
import com.qiuyj.qrpc.server.netty.RpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author qiuyj
 * @since 2018-09-02
 */
public class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final NettyConnection nettyConnection;

  public NettyClientChannelInitializer(NettyConnection nettyConnection) {
    this.nettyConnection = nettyConnection;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    // rpc消息解码器
    ch.pipeline().addLast("rpcMessageDecoder", new RpcMessageDecoder());
    // rpc消息编码器
    ch.pipeline().addLast("rpcMessageEncoder", new RpcMessageEncoder());
    // rpc错误消息处理器
    ch.pipeline().addLast("rpcErrorMessageHandler", new ErrorResponseHandler());
    // rpc客户端心跳处理器
    ch.pipeline().addLast("rpcClientHeartbeatHandler", new ClientHeartbeatHandler());
    // rpc正常消息处理器
    ch.pipeline().addLast("rpcClientMessageHandler", new NettyRpcClientMessageHandler(nettyConnection));
  }
}
