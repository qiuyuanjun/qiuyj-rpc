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
    ch.pipeline().addLast("rpcMessageDecoder", new RpcMessageDecoder());
    ch.pipeline().addLast("rpcMessageEncoder", new RpcMessageEncoder());
    ch.pipeline().addLast("rpcClientMessageHandler", new NettyRpcClientMessageHandler(nettyConnection));
  }
}
