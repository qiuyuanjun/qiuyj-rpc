package com.qiuyj.test.service;

import com.qiuyj.qrpc.client.ConfigurableRpcClient;
import com.qiuyj.qrpc.client.netty.NettyRpcClient;
import com.qiuyj.qrpc.commons.async.DefaultFuture;

import java.util.concurrent.ExecutionException;

/**
 * @author qiuyj
 * @since 2018-08-29
 */
public class ClientTest {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    ConfigurableRpcClient<TestService> rpcClient = new NettyRpcClient<>(TestService.class);
    rpcClient.setMaxRetryWhenFailedToConnect(3);
    rpcClient.connect();
    TestService testService = rpcClient.getServiceInstance();
    System.out.println(testService.toString());
    System.out.println(testService.hashCode());
    DefaultFuture<String> future = new DefaultFuture<>();
    new Thread(() -> future.setSuccess(testService.sayHello())).start();
    System.out.println(future.get());
    rpcClient.close();
  }
}
