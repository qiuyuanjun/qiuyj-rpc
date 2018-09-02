package com.qiuyj.qrpc.codec;

import com.qiuyj.qrpc.codec.hessian.Hessian2Codec;

/**
 * @author qiuyj
 * @since 2018-06-21
 */
public abstract class CodecUtils {

  public static Codec DEFAULT_CODEC = new Hessian2Codec();

  /**
   * 将字节数组转成整形
   * @param one 整形高25-32位所对应的字节
   * @param two 整形高17-24位所对应的字节
   * @param three 整形低9-16位所对应的字节
   * @param four 整形低1-8位所对应的字节
   * @return 对应的整形
   */
  public static int b2i(byte one, byte two, byte three, byte four) {
    return (one & 0xFF) << 24
        | (two & 0xFF) << 16
        | (three & 0xFF) << 8
        | (four & 0xFF);
  }

  /**
   * 将整数转成字节数组
   * @param i 整数
   * @return 对应的字节数组
   */
  public static byte[] i2b(int i) {
    return new byte[] {
        (byte) (i >>> 24),
        (byte) (i >>> 16),
        (byte) (i >>> 8),
        (byte) i
    };
  }

}