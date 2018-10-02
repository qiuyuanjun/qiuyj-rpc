/**
 * @author qiuyj
 * @since 2018-06-19
 */
module qrpc.commons {
  requires qiuyj.commons;
  requires org.slf4j;
  requires org.objectweb.asm;

  exports com.qiuyj.qrpc.commons;
  exports com.qiuyj.qrpc.commons.annotation;
  exports com.qiuyj.qrpc.commons.instantiation;
  exports com.qiuyj.qrpc.commons.async;
}
