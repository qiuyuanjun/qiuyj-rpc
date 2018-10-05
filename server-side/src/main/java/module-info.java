/**
 * @author qiuyj
 * @since 2018-06-19
 */
module qrpc.server {
  requires api;
  requires qrpc.commons;
  requires org.slf4j;
  requires qrpc.codec;
  requires io.netty.all;
  requires qiuyj.commons;
  requires qrpc.registry;

  exports com.qiuyj.qrpc.server.annotation;
  exports com.qiuyj.qrpc.server.interceptor;
  exports com.qiuyj.qrpc.server;
  exports com.qiuyj.qrpc.server.netty;
}
