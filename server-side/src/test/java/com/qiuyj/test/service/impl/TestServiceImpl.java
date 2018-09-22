package com.qiuyj.test.service.impl;

import com.qiuyj.qrpc.server.annotation.RpcServiceImpl;
import com.qiuyj.test.service.TestService;

import java.util.concurrent.TimeUnit;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
@RpcServiceImpl
public class TestServiceImpl implements TestService {

  @Override
  public String sayHello(String arg) {
    try {
      TimeUnit.SECONDS.sleep(5L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("调用");
    return "hello " + arg;
  }
}
