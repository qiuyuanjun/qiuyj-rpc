package com.qiuyj.test.service.impl;

import com.qiuyj.qrpc.server.annotation.RpcServiceImpl;
import com.qiuyj.test.service.TestService;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
@RpcServiceImpl
public class TestServiceImpl implements TestService {

  @Override
  public String sayHello(String arg) {
    try {
      Thread.sleep(5000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("调用");
    return "hello " + arg;
  }
}
