package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.commons.ClassUtils;
import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.commons.protocol.RequestInfo;
import com.qiuyj.qrpc.server.ServiceExporter;
import com.qiuyj.qrpc.server.invoke.ServiceProxy;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public class RequestInfoMessageHandler extends AbstractMessageHandler<RequestInfo> {

  private final ServiceExporter serviceExporter;

  public RequestInfoMessageHandler(ServiceExporter serviceExporter) {
    super(null);
    this.serviceExporter = serviceExporter;
  }

  @Override
  protected Object doHandle(RequestInfo message) {
    Class<?> interfaceCls = getInterfaceClass(message);
    ServiceProxy serviceProxy = serviceExporter.getServiceProxy(interfaceCls);
    // 这里无需判断serviceProxy为null的情况
    // 因为如果能到这里，那么表示一定不为null
    // 因为再调用这个方法之前调用的isAsyncExecute方法已经判断了
    return serviceProxy.call(message.getRequestId(), message.getMethodName(), message.getMethodParameters());
  }

  @Override
  protected boolean isAsyncExecute(RequestInfo message) {
    Class<?> interfaceCls = getInterfaceClass(message);
    return serviceExporter.isAsyncRequest(interfaceCls, message);
  }

  /**
   * 得到当前请求的服务接口的{@code Class}对象
   * @param message 消息请求体
   */
  private Class<?> getInterfaceClass(RequestInfo message) {
    String interfaceName = message.getInterfaceName();
    Class<?> interfaceCls;
    try {
      interfaceCls = ClassUtils.classForName(interfaceName, RequestInfoMessageHandler.class.getClassLoader());
    }
    catch (ClassNotFoundException e) {
      throw new RpcException(message.getRequestId(), ErrorReason.SERVICE_NOT_FOUND);
    }
    return interfaceCls;
  }
}