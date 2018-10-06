package com.qiuyj.test.service;

import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import com.qiuyj.qrpc.commons.annotation.RpcService;

/**
 * @author qiuyj
 * @since 2018-09-21
 */
@RpcService(application = "test")
public interface TestService {

  @RpcMethod(async = true)
  String sayHello(String arg);
}
