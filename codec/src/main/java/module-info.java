/**
 * @author qiuyj
 * @since 2018-06-20
 */
module qrpc.codec {

  requires com.caucho.hessian;
  requires org.slf4j;
  requires io.netty.all;
  requires qrpc.commons;

  exports com.qiuyj.qrpc.codec;
  exports com.qiuyj.qrpc.codec.hessian;
  exports com.qiuyj.qrpc.codec.protocol;
  exports com.qiuyj.qrpc.codec.protocol.heartbeat;
  exports com.qiuyj.qrpc.codec.netty;
}