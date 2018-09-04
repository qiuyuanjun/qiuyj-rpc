package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.server.AbstractRpcServer;
import com.qiuyj.qrpc.server.ServiceExporter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-06-19
 */
public class NettyRpcServer extends AbstractRpcServer {

  private ServerBootstrap nettyServerBootstrap;

  private NioServerSocketChannel serverChannel;

  private ChannelGroup clients;

  private final boolean sync;

  public NettyRpcServer() {
    this(false);
  }

  public NettyRpcServer(boolean sync) {
    this.sync = sync;
  }

  @Override
  protected void startInternal(ServiceExporter serviceExporter) {
    nettyServerBootstrap = createAndInitServerBootstrap(serviceExporter);
    // sync表示一直等待future，直到future完成了bind操作
    ChannelFuture channelFuture = nettyServerBootstrap.bind(getPort()).syncUninterruptibly();
    serverChannel = (NioServerSocketChannel) channelFuture.channel();
    // 得到所有的客户端长连接
    clients = ((NettyRpcChannelInitializer) nettyServerBootstrap.config().childHandler()).getClients();
  }

  @Override
  protected void closeInternal() {
    if (Objects.nonNull(nettyServerBootstrap)) {
      // 优雅的关闭线程池
      nettyServerBootstrap.config().childGroup().shutdownGracefully();
      nettyServerBootstrap.config().group().shutdownGracefully();
      nettyServerBootstrap = null;
    }
    if (Objects.nonNull(clients)) {
      // 关闭所有的客户端长连接
      if (!clients.isEmpty()) {
        clients.close().syncUninterruptibly();
        clients.clear();
      }
      clients = null;
    }
    if (Objects.nonNull(serverChannel)) {
      // 关闭channel
      serverChannel.close().syncUninterruptibly();
      serverChannel = null;
    }
  }

  @Override
  protected void afterStartedServer() {
    if (sync) {
      try {
        // 一直阻塞到服务器被关闭
        serverChannel.closeFuture().sync();
      }
      catch (InterruptedException e) {
        close(); // 关闭服务器
        throw new IllegalStateException(e);
        // ignore, do nothing
      }
    }
  }

  private static ServerBootstrap createAndInitServerBootstrap(ServiceExporter serviceExporter) {
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    // 工作线程默认采用 cpu * 2（以后应该扩展成读取配置文件的值）
    serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
        // 以下两个属性，server端不支持设置，client才支持设置
//        .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
//        .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
        .childHandler(new NettyRpcChannelInitializer(serviceExporter));
    return serverBootstrap;
  }
}
