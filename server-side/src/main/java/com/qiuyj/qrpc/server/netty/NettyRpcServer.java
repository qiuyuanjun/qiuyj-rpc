package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.server.AbstractRpcServer;
import com.qiuyj.qrpc.server.invoke.ServiceExporter;
import com.qiuyj.qrpc.commons.async.AsyncServiceCallExecutor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author qiuyj
 * @since 2018-06-19
 */
public class NettyRpcServer extends AbstractRpcServer {

  /** netty服务器端启动类 */
  private ServerBootstrap nettyServerBootstrap;

  /** 服务器端channel */
  private NioServerSocketChannel serverChannel;

  private ChannelGroup clients;

  /** 服务器端启动之后是否阻塞启动线程，直到{@code serverChannel}关闭 */
  private final boolean sync;

  public NettyRpcServer() {
    this(false);
  }

  public NettyRpcServer(boolean sync) {
    this.sync = sync;
  }

  @Override
  protected void startInternal(ServiceExporter serviceExporter) {
    ExecutorService asyncExecutor = getAsyncExecutor();
    if (Objects.isNull(asyncExecutor)) {
      asyncExecutor = new AsyncServiceCallExecutor();
      setAsyncExecutor(asyncExecutor);
    }
    nettyServerBootstrap = createAndInitServerBootstrap(asyncExecutor, serviceExporter);
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
  protected void afterServerStarted() {
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

  private static ServerBootstrap createAndInitServerBootstrap(ExecutorService asyncExecutor, ServiceExporter serviceExporter) {
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    // 工作线程默认采用 cpu * 2（以后应该扩展成读取配置文件的值）
    serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
        // 服务器端待接收客户端的连接的队列长度，超过这个数量的客户端连接会抛出异常
        .option(ChannelOption.SO_BACKLOG, 1024)
        // 支持长连接
        .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
        // 禁止nagel算法，防止tcp粘包
        .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
        .childHandler(new NettyRpcChannelInitializer(asyncExecutor, serviceExporter));
    return serverBootstrap;
  }
}
