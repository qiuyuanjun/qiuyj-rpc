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
System.out.println(testService.sayHello()); // hello world， hello qrpc
```

#### 接下来的目标
目前该rpc框架仅仅实现了服务的同步调用，接下来打算实现服务的异步调用，并且完善部分代码，优化部分代码