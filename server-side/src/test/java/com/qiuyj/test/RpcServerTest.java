package com.qiuyj.test;

import com.qiuyj.qrpc.server.ConfigurableRpcServer;
import com.qiuyj.qrpc.server.netty.NettyRpcServer;
import org.junit.Test;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
public class RpcServerTest {

  @Test
  public void test01() {
    ConfigurableRpcServer server = new NettyRpcServer(true);
    server.setServicePackageToScan("com.qiuyj.test.service");
    server.start();
  }

}