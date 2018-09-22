# 服务接口及实现类（位于服务器端）
```
@RpcService
public interface TestService {
  
  @RpcMethod(async = true)
  String sayHello(String arg);
}

public class TestServiceImpl implements TestService {
  
  @Override
  public String sayHello(String arg) {
    try {
      TimeUnit.SECONDS.sleep(5L);
    } 
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("服务异步调用");
    return "hello " + arg;
  }
}
```

# 服务器端用法
```
ConfigurableRpcServer server = new NettyRpcServer(); // 创建一个rpc服务器
server.addServiceToExport(TestService.class, new TestServiceImpl()); // 暴露服务
server.start(); // 启动rpc服务器
```

# 客户端用法
```
ConfigurableRpcClient<TestService> client = new NettyRpcClient<>(TestService.class); // 创建一个rpc客户端，并指定服务接口
client.connect(); // 连接远程服务器
TestService testService = client.getServiceInstance(); // 通过动态代理创建接口实现类
testService.sayHello("qiuyuanjun"); // rpc异步调用，此时从服务器端返回的结果为null
// 异步调用，通过AsyncContext.getFutuer()方法获得接收服务器端数据返回的Future对象
// 并且给这个future对象增加一个监听器，监听服务调用成功并且返回数据之后打印
AsyncContext.getFuture().addListener(f -> {
  System.out.println(f.getNow());
  client.close(); // 关闭和服务器端之间的链路
});
System.out.println("123");

客户端输出结果：
123
大约过了5秒之后，输出hello qiuyuanjun
```

#### 接下来的目标
目前该rpc框架仅仅实现了服务的同步调用和异步调用，默认为同步调用，接下来打算实现服务治理功能，并且完善部分代码，优化部分代码