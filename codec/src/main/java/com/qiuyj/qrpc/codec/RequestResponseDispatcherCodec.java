package com.qiuyj.qrpc.codec;

import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.codec.protocol.ResponseInfo;
import com.qiuyj.qrpc.codec.protocol.RpcMessage;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
public abstract class RequestResponseDispatcherCodec implements Codec {

  @Override
  public RpcMessage decode(byte[] b) {
    RpcMessage rpcMessage = new RpcMessage();
    // 读取魔数
    rpcMessage.setMagic(RpcProtocolUtils.readMagicNumber(b));
    // 读取报文类型，注意，报文类型这里仅仅是为了帮助判断
    rpcMessage.setMessageType(RpcProtocolUtils.readMessageType(b));
    // 读取正文长度
    rpcMessage.setContentLength(RpcProtocolUtils.readContentLength(b));
    // 如果有正文，那就读取正文
    if (rpcMessage.getContentLength() != 0) {
      // 正文是从第9个下标开始的
      if (rpcMessage.getMessageType().isRequestType()) {
        rpcMessage.setContent(decodeRequestInfo(b, RpcMessage.HEADER_LENGTH, rpcMessage.getContentLength()));
      }
      else if (rpcMessage.getMessageType().isResponseType()) {
        rpcMessage.setContent(decodeResponseInfo(b, RpcMessage.HEADER_LENGTH, rpcMessage.getContentLength()));
      }
    }
    return rpcMessage;
  }

  /**
   * 将字节数组的对应部分解码成{@code RequestInfo}对象
   * @param b 字节数组
   * @param startPos 开始位置
   * @param length 长度
   * @return 对应的{{@code RequestInfo}对象
   */
  protected abstract RequestInfo decodeRequestInfo(byte[] b, int startPos, int length);

  /**
   * 将字节数组的对应部分解码成{@code ResponseInfo}对象
   * @param b 字节数组
   * @param startPos 开始位置
   * @param length 长度
   * @return 对应的{{@code ResponseInfo}对象
   */
  protected abstract ResponseInfo decodeResponseInfo(byte[] b, int startPos, int length);

  @Override
  public byte[] encode(RpcMessage message) {
    byte[] content;
    if (message.getMessageType().isRequestType()) {
      content = encodeRequestInfo((RequestInfo) message.getContent());
    }
    else if (message.getMessageType().isResponseType()) {
      content = encodeResponseInfo((ResponseInfo) message.getContent());
    }
    else {
      content = new byte[0];
    }
    int contentLength = content.length,
        totalLength = RpcMessage.HEADER_LENGTH + contentLength;
    byte[] totalBytes = new byte[totalLength];
    System.arraycopy(CodecUtils.i2b(message.getMagic()), 0, totalBytes, 0, 4);
    totalBytes[4] = message.getMessageType().getMessageTypeByte();
    System.arraycopy(CodecUtils.i2b(contentLength), 0, totalBytes, 5, 4);
    if (contentLength > 0) {
      System.arraycopy(content, 0, totalBytes, RpcMessage.HEADER_LENGTH, contentLength);
    }
    return totalBytes;
  }

  /**
   * 将{@code RequestInfo}对象编码成对应的字节数组
   * @param requestInfo 要编码的{@code RequestInfo}对象
   * @return 对应的字节数组
   */
  protected abstract byte[] encodeRequestInfo(RequestInfo requestInfo);

  /**
   * 将{@code ResponseInfo}对象编码成对应的字节数组
   * @param responseInfo 要编码的{@code ResponseInfo}对象
   * @return 对应的字节数组
   */
  protected abstract byte[] encodeResponseInfo(ResponseInfo responseInfo);
}
