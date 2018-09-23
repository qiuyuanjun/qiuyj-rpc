package com.qiuyj.test;

import com.qiuyj.qrpc.server.ConfigurableRpcServer;
import com.qiuyj.qrpc.server.netty.NettyRpcServer;
import com.qiuyj.test.service.TestService;
import com.qiuyj.test.service.impl.TestServiceImpl;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
public class RpcServerTest {

  public static void main(String[] args) {
    ConfigurableRpcServer server = new NettyRpcServer();
    server.setServicePackageToScan("com.qiuyj.test.service");
    server.addServiceToExport(TestService.class, new TestServiceImpl());
    server.start();
  }

}