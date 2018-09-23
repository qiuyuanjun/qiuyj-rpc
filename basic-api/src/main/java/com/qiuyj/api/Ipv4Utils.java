package com.qiuyj.api;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author qiuyj
 * @since 2018-09-23
 */
public abstract class Ipv4Utils {

  /** IP地址匹配规则 */
  private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

  private static final String LOCAL_ADDRESS;
  static {
    InetAddress inetAddress = null;
    try {
      inetAddress = InetAddress.getLocalHost();
    }
    catch (UnknownHostException e) {
      // ignore
    }
    if (Objects.nonNull(inetAddress) && isValidAddress(inetAddress)) {
      LOCAL_ADDRESS = inetAddress.getHostAddress();
    }
    else {
      LOCAL_ADDRESS = getFirstValidLocalAddress();
    }
  }

  /**
   * 得到本机的ip地址
   */
  public static String getLocalAddress() {
    return LOCAL_ADDRESS;
  }

  /**
   * 得到本机的第一个有效的ip地址
   */
  public static String getFirstValidLocalAddress() {
    InetAddress inetAddress = null;
    try {
      inetAddress = NetworkInterface.networkInterfaces()
          .flatMap(NetworkInterface::inetAddresses)
          .filter(addr -> !addr.isLoopbackAddress() && !addr.getHostAddress().contains(":"))
          .findFirst()
          .orElse(null);
    }
    catch (SocketException e) {
      // ignore
    }
    return Objects.isNull(inetAddress) ? "127.0.0.1" : inetAddress.getHostAddress();
  }

  /**
   * 判断是否是一个合法的ip
   * @param inetAddress 要判断的ip地址
   * @return 如果是一个合法的，那么返回{@code true}，否则返回{@code false}
   */
  private static boolean isValidAddress(InetAddress inetAddress) {
    if (inetAddress.isLoopbackAddress()) {
      return false;
    }
    String host = inetAddress.getHostAddress();
    return Objects.nonNull(host) &&
        !"0.0.0.0".equals(host) &&
        !IP_PATTERN.matcher(host).matches();
  }

}