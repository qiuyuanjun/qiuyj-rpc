/**
 * @author qiuyj
 * @since 2018-08-29
 */
module qrpc.client {
  requires api;
  requires qrpc.commons;
  requires io.netty.all;
  requires qrpc.codec;

  exports com.qiuyj.qrpc.client;
  exports com.qiuyj.qrpc.client.netty;
}