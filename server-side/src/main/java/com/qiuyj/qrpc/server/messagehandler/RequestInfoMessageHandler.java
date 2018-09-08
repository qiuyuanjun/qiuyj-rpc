package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.commons.ClassUtils;
import com.qiuyj.qrpc.commons.protocol.RequestInfo;
import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.server.ServiceExporter;
import com.qiuyj.qrpc.server.invoke.ServiceProxy;

import java.util.Objects;

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
    String interfaceName = message.getInterfaceName();
    Class<?> interfaceCls;
    try {
      interfaceCls = ClassUtils.classForName(interfaceName, RequestInfoMessageHandler.class.getClassLoader());
    }
    catch (ClassNotFoundException e) {
      throw new RpcException(message.getRequestId(), ErrorReason.SERVICE_NOT_FOUND);
    }
    ServiceProxy serviceProxy = serviceExporter.getServiceProxy(interfaceCls);
    if (Objects.isNull(serviceProxy)) {
      throw new RpcException(message.getRequestId(), ErrorReason.SERVICE_NOT_FOUND);
    }
    return serviceProxy.call(message.getRequestId(), message.getMethodName(), message.getMethodParameters());
  }
}
