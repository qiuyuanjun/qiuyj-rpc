package com.qiuyj.qrpc.client;

import com.qiuyj.qrpc.commons.protocol.ResponseInfo;

import java.util.Objects;
import java.util.concurrent.*;

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
      responseHolderMap.remove(result.getRequestId());
      throw new IllegalStateException("Countdown latch not set yet.");
    }
    latch.countDown();
  }

  /**
   * 等待服务器端返回结果
   */
  public void waitForResponseResult(String requestId) throws TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    ResponseHolder holder = new ResponseHolder();
    holder.setLatch(latch);
    responseHolderMap.put(requestId, holder);
    try {
      // 如果此时，从netty服务器异步返回数据的时候抛出了异常
      // 那么这里将无法唤醒
      // 所以这里设置一个最长的阻塞时间5秒
      // TODO timeout通过用户配置的读取
      latch.await(5L, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    // 这里需要检测是否有值，如果没有值
    // 那么表明抛出了异常
    // 此时这里需要手动向外部抛出一个异常，并且将当前的responseHolder移除
    if (Objects.isNull(responseHolderMap.get(requestId).getResponse())) {
      responseHolderMap.remove(requestId);
      throw new TimeoutException("Read from server timeout. May be occured exception.");
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
