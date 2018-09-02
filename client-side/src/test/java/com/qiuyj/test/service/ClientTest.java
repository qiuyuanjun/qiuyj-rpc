package com.qiuyj.test.service;

import com.qiuyj.qrpc.client.ConfigurableRpcClient;
import com.qiuyj.qrpc.client.netty.NettyRpcClient;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public class ClientTest {

  @Test
  public void testClient() {
    ConfigurableRpcClient<TestService> client = new NettyRpcClient<>();
    client.setMaxRetryWhenFailedToConnect(3);
    client.setServiceInterface(TestService.class);
    client.connect();
    TestService testService = client.getServiceInstance();
    System.out.println(testService.sayHello());
  }
}
