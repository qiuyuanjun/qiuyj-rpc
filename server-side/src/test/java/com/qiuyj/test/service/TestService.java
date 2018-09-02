package com.qiuyj.test.service;

import com.qiuyj.qrpc.commons.annotation.RpcService;
import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import com.qiuyj.test.service.impl.TestServiceImpl;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
@RpcService(implementation = TestServiceImpl.class)
public interface TestService {

  @RpcMethod
  String sayHello();

}