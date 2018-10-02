package com.qiuyj.qrpc.server.messagehandler;

import com.qiuyj.commons.ClassUtils;
import com.qiuyj.qrpc.commons.ErrorReason;
import com.qiuyj.qrpc.commons.RpcException;
import com.qiuyj.qrpc.codec.protocol.RequestInfo;
import com.qiuyj.qrpc.server.ServiceExporter;
import com.qiuyj.qrpc.server.invoke.ServiceProxy;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author qiuyj
 * @since 2018-08-26
 */
public class RequestInfoMessageHandler extends AbstractMessageHandler<RequestInfo> {

  /**
   * 当前rpc服务器所暴露的所有的服务
   */
  private final ServiceExporter serviceExporter;

  public RequestInfoMessageHandler(ExecutorService asyncExecutor, ServiceExporter serviceExporter) {
    super(null, asyncExecutor);
    this.serviceExporter = serviceExporter;
  }

  @Override
  protected Object doHandle(RequestInfo message) {
    Class<?> interfaceCls = getInterfaceClass(message);
    ServiceProxy serviceProxy = serviceExporter.getServiceProxy(interfaceCls);
    // 这里仍然需要判断serviceProxy为null的情况
    // 如果当前请求不是异步请求的话，那么在isAsyncExecute方法内部不会去判断serviceProxy为null
    // 所以这里还需要再次判断，确保程序的健壮性
    if (Objects.isNull(serviceProxy)) {
      throw new RpcException(message.getRequestId(), ErrorReason.SERVICE_NOT_FOUND);
    }
    return serviceProxy.call(message.getRequestId(), message.getMethodName(), message.getMethodParameters());
  }

  @Override
  protected boolean isAsyncExecute(RequestInfo message) {
    if (!isAsyncRequest(message)) {
      return false;
    }
    Class<?> interfaceCls = getInterfaceClass(message);
    return serviceExporter.isAsyncRequest(interfaceCls, message);
  }

  /**
   * 判断当前请求是否是异步请求，子类覆盖并重写该方法
   * @param requestInfo 请求对象
   * @return 如果是异步请求，返回{@code true}，否则返回{@code false}
   */
  protected boolean isAsyncRequest(RequestInfo requestInfo) {
    return true;
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
