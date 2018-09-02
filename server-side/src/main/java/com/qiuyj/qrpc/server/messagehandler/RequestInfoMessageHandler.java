package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.commons.ClassUtils;
import com.qiuyj.qrpc.codec.RequestInfo;
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
      throw new RpcException();
    }
    ServiceProxy serviceProxy = serviceExporter.getServiceProxy(interfaceCls);
    if (Objects.isNull(serviceProxy)) {
      throw new RpcException();
    }
    return serviceProxy.invoke(message.getMethodName(), message.getMethodParameters());
  }
}
