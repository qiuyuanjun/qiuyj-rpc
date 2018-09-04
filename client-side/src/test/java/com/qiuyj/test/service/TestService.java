package com.qiuyj.test.service;

import com.qiuyj.qrpc.commons.annotation.RpcMethod;
import com.qiuyj.qrpc.commons.annotation.RpcService;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
@RpcService
public interface TestService {

  @RpcMethod
  String sayHello();

  void notExists();

}