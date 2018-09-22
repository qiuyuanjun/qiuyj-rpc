package com.qiuyj.qrpc.codec.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.codec.RequestResponseDispatcherCodec;
import com.qiuyj.qrpc.codec.protocol.ResponseInfo;
import com.qiuyj.qrpc.codec.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author qiuyj
 * @since 2018-06-22
 */
public class Hessian2Codec extends RequestResponseDispatcherCodec {

  @Override
  protected RequestInfo decodeRequestInfo(byte[] b, int startPos, int length) {
    Hessian2Input hessian2Input = new HessianSerializerInput(new ByteArrayInputStream(b, startPos, length));
    try {
      return decodeObject(hessian2Input, RequestInfo.class);
    }
    finally {
      try {
        hessian2Input.close();
      }
      catch (IOException e) {
        // ignore
      }
    }
  }

  @Override
  protected ResponseInfo decodeResponseInfo(byte[] b, int startPos, int length) {
    Hessian2Input hessian2Input = new HessianSerializerInput(new ByteArrayInputStream(b, startPos, length));
    try {
      return decodeObject(hessian2Input, ResponseInfo.class);
    }
    finally {
      try {
        hessian2Input.close();
      }
      catch (IOException e) {
        // ignore
      }
    }
  }

  private static <T> T decodeObject(Hessian2Input in, Class<T> clz) {
    Object o;
    try {
      o = in.readObject(clz);
    }
    catch (IOException e) {
      throw new SerializationException(e);
    }
    return clz.cast(o);
  }

  @Override
  protected byte[] encodeRequestInfo(RequestInfo requestInfo) {
    return encodeObject(requestInfo);
  }

  @Override
  protected byte[] encodeResponseInfo(ResponseInfo responseInfo) {
    return encodeObject(responseInfo);
  }

  private static byte[] encodeObject(Object obj) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    Hessian2Output hessian2Output = new HessianSerializerOutput(out);
    try {
      hessian2Output.writeObject(obj);
      hessian2Output.flush();
      return out.toByteArray();
    }
    catch (IOException e) {
      throw new SerializationException(e);
    }
    finally {
      try {
        hessian2Output.close();
      }
      catch (IOException e) {
        // ignore
      }
    }
  }
}