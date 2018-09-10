/**
 * @author qiuyj
 * @since 2018-06-19
 */
module qrpc.server {
  requires io.netty.all;
  requires api;
  requires qiuyj.commons;
  requires qrpc.commons;
  requires qrpc.codec;
  requires org.slf4j;

  exports com.qiuyj.qrpc.server.annotation;
  exports com.qiuyj.qrpc.server.interceptor;
  exports com.qiuyj.qrpc.server;
  exports com.qiuyj.qrpc.server.invoke;
  exports com.qiuyj.qrpc.server.netty;
}