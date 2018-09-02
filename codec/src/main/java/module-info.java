/**
 * @author qiuyj
 * @since 2018-06-20
 */
module qrpc.codec {

  requires com.caucho.hessian;
  requires qrpc.commons;

  exports com.qiuyj.qrpc.codec;
  exports com.qiuyj.qrpc.codec.hessian;
}