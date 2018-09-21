package com.qiuyj.test.service;

import com.qiuyj.qrpc.commons.annotation.RpcMethod;

/**
 * @author qiuyj
 * @since 2018-09-21
 */
public interface TestService {

  @RpcMethod(async = true)
  String sayHello(String arg);
}
