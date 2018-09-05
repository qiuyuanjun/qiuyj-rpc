package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.server.ServiceExporter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author qiuyj
 * @since 2018-06-19
 */
public class NettyRpcChannelInitializer extends ChannelInitializer<SocketChannel> {

  /** 客户端的长连接 ip -> socketchannel，全局唯一 */
  private final ChannelGroup clients;

  /** 当前服务器所有暴露的服务的集合 */
  private final ServiceExporter serviceExporter;

  public NettyRpcChannelInitializer(ServiceExporter serviceExporter) {
    this.serviceExporter = serviceExporter;
    clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    // 消息解码器
    ch.pipeline().addLast("rpcMessageDecoder", new RpcMessageDecoder());
    // 消息编码器
    ch.pipeline().addLast("rpcMessageEncoder", new RpcMessageEncoder());
    // 心跳服务器端处理器
    ch.pipeline().addLast("heartbeatServerHandler", new HeartbeatServerHandler());
    // rpc调用处理器
    ch.pipeline().addLast("rpcMessageHandler", new NettyRpcInvokerHandler(clients, serviceExporter));
  }

  public ChannelGroup getClients() {
    return clients;
  }
}
