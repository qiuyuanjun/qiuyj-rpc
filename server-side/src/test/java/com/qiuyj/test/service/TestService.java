package com.qiuyj.test.service;

import com.qiuyj.qrpc.commons.annotation.RpcService;
import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import com.qiuyj.test.service.impl.TestServiceImpl;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
@RpcService
public interface TestService {

  @RpcMethod(async = true)
  String sayHello(String arg);

}