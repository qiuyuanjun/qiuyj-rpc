package com.qiuyj.qrpc.client;

import com.qiuyj.qrpc.codec.ResponseInfo;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author qiuyj
 * @since 2018-09-02
 */
public class ResponseManager {

  public static final ResponseManager INSTANCE = new ResponseManager();

  /** requestId -> responseHolder */
  private final ConcurrentMap<String, ResponseHolder> responseHolderMap = new ConcurrentHashMap<>();

  private ResponseManager() {
    // for priavte
  }

  /**
   * 完成,设置结果，并通知放行
   * @param result 结果
   */
  public void done(ResponseInfo result) {
    ResponseHolder holder = responseHolderMap.get(result.getRequestId());
    if (Objects.isNull(holder)) {
      throw new IllegalStateException("Holder not set yet.");
    }
    holder.setResponse(result);
    CountDownLatch latch = holder.getLatch();
    if (Objects.isNull(latch)) {
      throw new IllegalStateException("Countdown latch not set yet.");
    }
    latch.countDown();
  }

  /**
   * 等待服务器端返回结果
   */
  public void waitForResponseResult(String requestId) {
    CountDownLatch latch = new CountDownLatch(1);
    ResponseHolder holder = new ResponseHolder();
    holder.setLatch(latch);
    responseHolderMap.put(requestId, holder);
    try {
      latch.await(5L, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * 同步得到服务器端返回的结果
   * @return 结果
   */
  public ResponseInfo getResult(String requestId) {
    // 从原来的容器里面删除
    ResponseHolder responseHolder = responseHolderMap.remove(requestId);
    if (Objects.isNull(responseHolder)) {
      throw new IllegalStateException("Holder not set yet.");
    }
    else if (Objects.isNull(responseHolder.getResponse())) {
      throw new IllegalStateException("Result not set yet.");
    }
    return responseHolder.getResponse();
  }

  private static class ResponseHolder {

    private ResponseInfo response;

    private CountDownLatch latch;

    public ResponseInfo getResponse() {
      return response;
    }

    public void setResponse(ResponseInfo response) {
      this.response = response;
    }

    public CountDownLatch getLatch() {
      return latch;
    }

    public void setLatch(CountDownLatch latch) {
      this.latch = latch;
    }
  }
}
