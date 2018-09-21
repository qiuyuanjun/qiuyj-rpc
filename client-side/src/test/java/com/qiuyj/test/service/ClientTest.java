package com.qiuyj.test.service;

import com.qiuyj.qrpc.client.AsyncContext;
import com.qiuyj.qrpc.client.ConfigurableRpcClient;
import com.qiuyj.qrpc.client.netty.NettyRpcClient;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public class ClientTest {

  public static void main(String[] args) {
    ConfigurableRpcClient<TestService> rpcClient = new NettyRpcClient<>(TestService.class);
    rpcClient.setMaxRetryWhenFailedToConnect(3);
    rpcClient.connect();
    TestService testService = rpcClient.getServiceInstance();
    testService.sayHello("qiuyj");
    AsyncContext.getFuture().addListener(f -> System.out.println(f.getNow()));
    System.out.println(123);
  }
}
