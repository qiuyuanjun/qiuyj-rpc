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
  public String sayHello() {
    return "hello world， hello qrpc";
  }
}
