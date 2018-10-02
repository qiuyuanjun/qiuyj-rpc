package com.qiuyj.qrpc.client.netty;

import com.qiuyj.api.Connection;
import com.qiuyj.qrpc.client.AbstractRpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public class NettyRpcClient<T> extends AbstractRpcClient<T> {

  private Bootstrap bootstrap;

  private SocketChannel socketChannel;

  public NettyRpcClient() {
    // call setServiceInterface(Class<?> serviceInterface) method later
  }

  public NettyRpcClient(Class<T> serviceInterface) {
    super(serviceInterface);
  }

  @Override
  protected Connection doConnect() {
    super.doConnect();
    NettyConnection nettyConnection = new NettyConnection(this);
    bootstrap = createAndInitBootstrap(nettyConnection);
    // 连接远程服务器
    ChannelFuture future = bootstrap.connect(getRemoteServerAddress()).syncUninterruptibly();
    socketChannel = (SocketChannel) future.channel();
    // 将channel设置到connection里面
    nettyConnection.setSocketChannel(socketChannel);
    return nettyConnection;
  }

  @Override
  protected void doClose() {
    super.doClose();
    // 关闭channel
    if (socketChannel.isOpen() || socketChannel.isActive()) {
      socketChannel.close().syncUninterruptibly();
    }
    socketChannel = null;
    // 关闭ExecutorService
    bootstrap.config().group().shutdownGracefully();
    bootstrap = null;
  }

  private static Bootstrap createAndInitBootstrap(NettyConnection nettyConnection) {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(new NioEventLoopGroup())
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
        .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
        .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
        .handler(new NettyClientChannelInitializer(nettyConnection));
    return bootstrap;
  }
}
