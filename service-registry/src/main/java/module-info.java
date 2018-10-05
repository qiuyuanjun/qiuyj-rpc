/**
 * @author qiuyj
 * @since 2018-09-23
 */
module qrpc.registry {
  requires curator.framework;
  requires curator.client;
  requires org.slf4j;
  requires zookeeper;

  exports com.qiuyj.qrpc.registry;
}
