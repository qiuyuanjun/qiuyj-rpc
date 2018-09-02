package com.qiuyj.test.service.impl;

import com.qiuyj.test.service.TestService;

/**
 * @author qiuyj
 * @since 2018-06-18
 */
public class TestServiceImpl implements TestService {

  @Override
  public String sayHello() {
    return "hello world， hello qrpc";
  }
}
