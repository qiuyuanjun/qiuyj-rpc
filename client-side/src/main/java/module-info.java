/**
 * @author qiuyj
 * @since 2018-08-29
 */
module qrpc.client {
  requires io.netty.all;
  requires api;
  requires qrpc.server;
  requires qrpc.commons;
  requires qrpc.codec;

  exports com.qiuyj.qrpc.client;
  exports com.qiuyj.qrpc.client.netty;
  exports com.qiuyj.qrpc.client.proxy;
}